package fr.insa.mesh.analysis.controller;

import fr.insa.mesh.analysis.model.AnalysisResult;
import fr.insa.mesh.analysis.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    /**
     * GET /health
     * Endpoint de liveness/readiness pour Kubernetes et docker-compose.
     * Retourne toujours 200 OK dès que le service est démarré.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "analysis-api"));
    }

    /**
     * POST /analyze/{id}
     * Lance l'analyse de l'échantillon.
     * Appel inter-service vers sample-api inclus.
     * Latence artificielle de 300 ms (démontre les retries Linkerd).
     */
    @PostMapping("/analyze/{id}")
    public ResponseEntity<AnalysisResult> analyze(@PathVariable String id) {
        try {
            AnalysisResult result = service.analyze(id);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /results/{id}
     * Retourne le résultat d'une analyse déjà effectuée.
     */
    @GetMapping("/results/{id}")
    public ResponseEntity<AnalysisResult> getResult(@PathVariable String id) {
        return service.getResult(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
