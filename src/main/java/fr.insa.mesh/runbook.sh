#!/usr/bin/env bash
# =============================================================================
# LaboTrack – RunBook
# =============================================================================
# Usage :
#   ./runbook.sh          → déploiement complet
#   ./runbook.sh clean    → suppression complète
#   ./runbook.sh status   → vérification de l'état
#   ./runbook.sh build    → build des images uniquement
#   ./runbook.sh deploy   → déploiement k8s uniquement (après build)
#   ./runbook.sh dashboard → ouvre les dashboards
# =============================================================================

set -euo pipefail

NAMESPACE="labotrack"
REGISTRY="labotrack"
VERSION="1.0"
SERVICES=("sample-api" "analysis-api" "result-frontend")
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── Couleurs ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; NC='\033[0m'

log()   { echo -e "${GREEN}[✔]${NC} $*"; }
info()  { echo -e "${BLUE}[ℹ]${NC} $*"; }
warn()  { echo -e "${YELLOW}[⚠]${NC} $*"; }
error() { echo -e "${RED}[✖]${NC} $*"; exit 1; }
step()  { echo -e "\n${CYAN}═══ $* ═══${NC}"; }

# =============================================================================
# ÉTAPE 0 : Vérifications préalables
# =============================================================================
check_prerequisites() {
    step "Vérification des prérequis"
    for cmd in minikube kubectl docker curl; do
        command -v "$cmd" &>/dev/null || error "$cmd non trouvé. Veuillez l'installer."
        log "$cmd : $(command -v $cmd)"
    done
}

# =============================================================================
# ÉTAPE 1 : Préparer Minikube
# =============================================================================
prepare_minikube() {
    step "Étape 1 – Préparation de Minikube"

    # Démarrage de Minikube (driver docker recommandé)
    if ! minikube status --profile minikube &>/dev/null 2>&1; then
        info "Démarrage de Minikube (driver=docker, 4 CPU, 6 Go RAM)..."
        minikube start --driver=docker --cpus=4 --memory=6144 --disk-size=20g
    else
        log "Minikube déjà démarré."
    fi

    # Activation des addons utiles
    info "Activation de l'addon metrics-server..."
    minikube addons enable metrics-server

    info "Activation de l'addon dashboard..."
    minikube addons enable dashboard

    info "Activation de l'addon registry..."
    minikube addons enable registry

    log "Minikube prêt. Statut :"
    minikube status

    # Pointer Docker vers le daemon Minikube pour le build
    info "Configuration de l'environnement Docker vers Minikube..."
    eval "$(minikube docker-env)"
    log "Docker pointe maintenant vers le daemon Minikube."
}

# =============================================================================
# ÉTAPE 2 : Installer le CLI Linkerd + pré-checks
# =============================================================================
install_linkerd_cli() {
    step "Étape 2 – Installation CLI Linkerd + pré-checks"

    if ! command -v linkerd &>/dev/null; then
        info "Téléchargement et installation du CLI Linkerd..."
        curl --proto '=https' --tlsv1.2 -sSfL https://run.linkerd.io/install | sh
        export PATH="$HOME/.linkerd2/bin:$PATH"
        echo 'export PATH="$HOME/.linkerd2/bin:$PATH"' >> ~/.zshrc
    else
        log "CLI Linkerd déjà installé : $(linkerd version --client)"
    fi

    info "Exécution des pré-checks Kubernetes..."
    linkerd check --pre
    log "Pré-checks Linkerd : OK"
}

# =============================================================================
# ÉTAPE 3 : Installer Linkerd (CRDs + control plane)
# =============================================================================
install_linkerd_control_plane() {
    step "Étape 3 – Installation Linkerd (CRDs + control plane)"

    if kubectl get namespace linkerd &>/dev/null 2>&1; then
        warn "Linkerd control plane déjà installé."
    else
        info "Installation des CRDs Linkerd..."
        linkerd install --crds | kubectl apply -f -

        info "Installation du control plane Linkerd..."
        linkerd install --set proxyInit.runAsRoot=true | kubectl apply -f -

        info "Attente que le control plane soit prêt..."
        linkerd check
        log "Linkerd control plane installé."
    fi

    # Installation de Linkerd Viz (observabilité)
    if ! kubectl get namespace linkerd-viz &>/dev/null 2>&1; then
        info "Installation de Linkerd Viz..."
        linkerd viz install | kubectl apply -f -
        kubectl rollout status deploy -n linkerd-viz --timeout=120s
        log "Linkerd Viz installé."
    else
        warn "Linkerd Viz déjà installé."
    fi
}

# =============================================================================
# ÉTAPE 4 : Build des images Docker + injection Linkerd
# =============================================================================
build_images() {
    step "Étape 4 – Build des images Docker (multi-stage)"

    # S'assurer que Docker pointe vers Minikube
    eval "$(minikube docker-env)"

    for service in "${SERVICES[@]}"; do
        info "Build de $service..."
        docker build \
            -t "${REGISTRY}/${service}:${VERSION}" \
            -f "${SCRIPT_DIR}/${service}/Dockerfile" \
            "${SCRIPT_DIR}/${service}"
        log "Image ${REGISTRY}/${service}:${VERSION} construite."
    done

    info "Images disponibles dans le daemon Minikube :"
    docker images | grep labotrack
}

# =============================================================================
# ÉTAPE 5 : Déploiement Kubernetes
# =============================================================================
deploy_kubernetes() {
    step "Étape 5 – Déploiement Kubernetes avec injection Linkerd"

    info "Application des manifests Kubernetes..."
    kubectl apply -f "${SCRIPT_DIR}/k8s/00-namespace.yaml"
    kubectl apply -f "${SCRIPT_DIR}/k8s/01-postgres.yaml"
    kubectl apply -f "${SCRIPT_DIR}/k8s/02-sample-api.yaml"
    kubectl apply -f "${SCRIPT_DIR}/k8s/03-analysis-api.yaml"
    kubectl apply -f "${SCRIPT_DIR}/k8s/04-result-frontend.yaml"

    info "Attente du démarrage de PostgreSQL..."
    kubectl rollout status deployment/postgres -n "$NAMESPACE" --timeout=120s

    info "Attente du démarrage de sample-api..."
    kubectl rollout status deployment/sample-api -n "$NAMESPACE" --timeout=120s

    info "Attente du démarrage de analysis-api..."
    kubectl rollout status deployment/analysis-api -n "$NAMESPACE" --timeout=120s

    info "Attente du démarrage de result-frontend..."
    kubectl rollout status deployment/result-frontend -n "$NAMESPACE" --timeout=120s

    log "Tous les pods sont démarrés."
    kubectl get pods -n "$NAMESPACE"
}

# =============================================================================
# ÉTAPE 6 : Appliquer les profils et politiques Linkerd
# =============================================================================
apply_linkerd_policies() {
    step "Étape 6 – Profils ServiceProfile + Politiques Zero-Trust"

    info "Application des ServiceProfiles (retries, timeouts)..."
    kubectl apply -f "${SCRIPT_DIR}/k8s/05-linkerd-profiles.yaml"

    info "Application des politiques ServerAuthorization (Zero-Trust mTLS)..."
    kubectl apply -f "${SCRIPT_DIR}/k8s/06-policies.yaml"

    log "Politiques Linkerd appliquées."

    info "Vérification Linkerd complète..."
    linkerd check
}

# =============================================================================
# ÉTAPE 7 (Optionnel) : Stack Prometheus + Grafana
# =============================================================================
deploy_monitoring() {
    step "Étape 7 (Optionnel) – Déploiement Prometheus + Grafana"

    info "Application du manifest Prometheus/Grafana..."
    kubectl apply -f "${SCRIPT_DIR}/k8s/07-prometheus-grafana.yaml"

    info "Attente de Prometheus..."
    kubectl rollout status deployment/prometheus -n "$NAMESPACE" --timeout=120s
    info "Attente de Grafana..."
    kubectl rollout status deployment/grafana -n "$NAMESPACE" --timeout=120s

    log "Stack de monitoring déployée."
    info "Sur macOS Docker driver, accéder à Grafana via port-forward :"
    info "  kubectl port-forward svc/grafana -n $NAMESPACE 3000:3000 &"
    info "  Ouvrir : http://localhost:3000  (admin / admin123)"
}

# =============================================================================
# Ouverture des dashboards
# =============================================================================
open_dashboards() {
    step "Ouverture des Dashboards"

    info "Dashboard Minikube (nouvel onglet) :"
    minikube dashboard &

    info "Dashboard Linkerd Viz (nouvel onglet) :"
    linkerd viz dashboard &

    info "URL du result-frontend (port-forward) :"
    kubectl port-forward svc/result-frontend -n "$NAMESPACE" 8082:8082 &>/dev/null &
    info "  http://localhost:8082"

    log "Dashboards ouverts."
}

# =============================================================================
# Vérification de l'état
# =============================================================================
check_status() {
    step "État du déploiement LaboTrack"

    echo ""
    info "=== Pods ==="
    kubectl get pods -n "$NAMESPACE" -o wide

    echo ""
    info "=== Services ==="
    kubectl get svc -n "$NAMESPACE"

    echo ""
    info "=== Linkerd – métriques de trafic ==="
    linkerd viz stat deploy -n "$NAMESPACE" 2>/dev/null || warn "Linkerd Viz non disponible"

    echo ""
    info "=== Linkerd – top (5 secondes) ==="
    timeout 5 linkerd viz top deploy -n "$NAMESPACE" 2>/dev/null || true

    echo ""
    info "=== Vérification mTLS ==="
    linkerd viz edges pod -n "$NAMESPACE" 2>/dev/null || warn "Linkerd Viz non disponible"
}

# =============================================================================
# Nettoyage
# =============================================================================
cleanup() {
    step "Nettoyage complet"
    warn "Suppression du namespace $NAMESPACE et de toutes ses ressources..."
    kubectl delete namespace "$NAMESPACE" --ignore-not-found=true

    warn "Désinstallation de Linkerd..."
    linkerd viz uninstall 2>/dev/null | kubectl delete -f - --ignore-not-found=true || true
    linkerd uninstall 2>/dev/null | kubectl delete -f - --ignore-not-found=true || true

    log "Nettoyage terminé."
}

# =============================================================================
# Tests fonctionnels rapides
# =============================================================================
smoke_test() {
    step "Tests fonctionnels (smoke tests)"

    # Sur macOS Docker driver les services ClusterIP ne sont pas accessibles via minikube ip
    # → on utilise kubectl port-forward
    info "Démarrage des tunnels port-forward..."
    kubectl port-forward svc/sample-api   -n "$NAMESPACE" 8080:8080 &>/dev/null &
    PF_SAMPLE=$!
    kubectl port-forward svc/analysis-api -n "$NAMESPACE" 8081:8081 &>/dev/null &
    PF_ANALYSIS=$!
    sleep 3

    SAMPLE_URL="http://localhost:8080"
    ANALYSIS_URL="http://localhost:8081"

    info "Test 1 – Enregistrement d'un échantillon (REGISTERED)..."
    SAMPLE=$(curl -s -X POST "${SAMPLE_URL}/samples" \
        -H "Content-Type: application/json" \
        -d '{"patientName":"Jean Test","examType":"Glycémie","sampleType":"Sang"}')
    echo "  → $SAMPLE"

    ID=$(echo "$SAMPLE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    [ -n "$ID" ] && log "Échantillon créé : ID=$ID" || warn "Impossible de récupérer l'ID"

    if [ -n "$ID" ]; then
        info "Test 2 – Consultation de l'échantillon $ID..."
        curl -s "${SAMPLE_URL}/samples/${ID}" | python3 -m json.tool 2>/dev/null || true

        info "Test 3 – Analyse via gRPC (PRE_ANALYSIS → IN_ANALYSIS → VALIDATED → COMPLETED)..."
        curl -s -X POST "${ANALYSIS_URL}/analyze/${ID}" | python3 -m json.tool 2>/dev/null || true

        info "Test 4 – Restitution du résultat..."
        curl -s "${ANALYSIS_URL}/results/${ID}" | python3 -m json.tool 2>/dev/null || true

        FINAL_STATUS=$(curl -s "${SAMPLE_URL}/samples/${ID}" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        [ "$FINAL_STATUS" = "COMPLETED" ] && log "Workflow complet : statut final = $FINAL_STATUS" \
                                           || warn "Statut inattendu : $FINAL_STATUS"
    fi

    kill "$PF_SAMPLE" "$PF_ANALYSIS" 2>/dev/null || true
}

# =============================================================================
# Point d'entrée
# =============================================================================
main() {
    case "${1:-all}" in
        all)
            check_prerequisites
            prepare_minikube
            install_linkerd_cli
            install_linkerd_control_plane
            build_images
            deploy_kubernetes
            apply_linkerd_policies
            deploy_monitoring
            step "Déploiement terminé !"
            check_status
            info "Ouvrir les dashboards : ./runbook.sh dashboard"
            info "Tester l'application  : ./runbook.sh test"
            ;;
        build)
            check_prerequisites
            build_images
            ;;
        deploy)
            check_prerequisites
            deploy_kubernetes
            apply_linkerd_policies
            ;;
        dashboard)
            open_dashboards
            ;;
        status)
            check_status
            ;;
        test)
            smoke_test
            ;;
        monitoring)
            deploy_monitoring
            ;;
        clean)
            cleanup
            ;;
        *)
            echo "Usage: $0 {all|build|deploy|dashboard|status|test|monitoring|clean}"
            exit 1
            ;;
    esac
}

main "$@"
