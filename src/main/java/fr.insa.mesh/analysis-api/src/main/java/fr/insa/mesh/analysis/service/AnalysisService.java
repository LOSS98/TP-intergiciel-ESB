package fr.insa.mesh.analysis.service;

import fr.insa.mesh.analysis.grpc.SampleGrpcClient;
import fr.insa.mesh.analysis.model.AnalysisResult;
import fr.insa.mesh.grpc.SampleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnalysisService {

    private final Map<String, AnalysisResult> resultStore = new ConcurrentHashMap<>();
    private final SampleGrpcClient sampleGrpcClient;
    private final long delayMs;
    private final Random random = new Random();

    private static final Map<String, double[]> RANGES = Map.of(
            "Glycémie",    new double[]{0.70, 1.10},
            "NFS",         new double[]{4.0,  10.0},
            "Créatinine",  new double[]{6.0,  13.0},
            "Cholestérol", new double[]{1.5,  2.0},
            "TSH",         new double[]{0.4,  4.0}
    );

    private static final Map<String, String> UNITS = Map.of(
            "Glycémie",    "g/L",
            "NFS",         "G/L",
            "Créatinine",  "mg/L",
            "Cholestérol", "g/L",
            "TSH",         "mUI/L"
    );

    public AnalysisService(
            SampleGrpcClient sampleGrpcClient,
            @Value("${analysis.processing.delay-ms:300}") long delayMs) {
        this.sampleGrpcClient = sampleGrpcClient;
        this.delayMs = delayMs;
    }

    /**
     * Cycle complet : Enregistrement → Pré-analyse → Analyse → Validation → Restitution.
     * Les transitions de statut passent par gRPC (sample-api).
     */
    public AnalysisResult analyze(String sampleId) {
        // ── Étape 1 : Récupération de l'échantillon via gRPC ────────────────
        SampleResponse sample = sampleGrpcClient.getSample(sampleId);

        // ── Étape 2 : Pré-analyse ────────────────────────────────────────────
        sampleGrpcClient.updateStatus(sampleId, "PRE_ANALYSIS");

        // ── Étape 3 : Analyse (latence artificielle, démontre les retries Linkerd)
        sampleGrpcClient.updateStatus(sampleId, "IN_ANALYSIS");
        simulateProcessing();

        // ── Génération du résultat ───────────────────────────────────────────
        String examType = sample.getExamType();
        double[] range  = RANGES.getOrDefault(examType, new double[]{0.5, 2.0});
        String unit     = UNITS.getOrDefault(examType, "unité");

        double minV = range[0] * 0.8;
        double maxV = range[1] * 1.2;
        double value = Math.round((minV + (maxV - minV) * random.nextDouble()) * 100.0) / 100.0;

        String interpretation;
        if (value < range[0])      interpretation = "BAS";
        else if (value > range[1]) interpretation = "ÉLEVÉ";
        else                       interpretation = "NORMAL";

        // ── Étape 4 : Validation biologique ─────────────────────────────────
        sampleGrpcClient.updateStatus(sampleId, "VALIDATED");

        AnalysisResult result = new AnalysisResult(
                sampleId,
                sample.getPatientName(),
                examType,
                value,
                unit,
                interpretation,
                LocalDateTime.now(),
                true,
                "Dr. Auto-Validator [" + sampleId + "]"
        );

        // ── Étape 5 : Restitution (COMPLETED) ───────────────────────────────
        sampleGrpcClient.updateStatus(sampleId, "COMPLETED");
        resultStore.put(sampleId, result);
        return result;
    }

    public Optional<AnalysisResult> getResult(String sampleId) {
        return Optional.ofNullable(resultStore.get(sampleId));
    }

    private void simulateProcessing() {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
