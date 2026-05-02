package fr.insa.mesh.analysis.model;

import java.time.LocalDateTime;

/**
 * Résultat d'une analyse biologique simulée.
 */
public record AnalysisResult(
        String sampleId,
        String patientName,
        String examType,
        double value,
        String unit,
        String interpretation,    // NORMAL | ELEVÉ | BAS
        LocalDateTime analyzedAt,
        boolean validated,
        String validatorSignature // simulation de visa biologique
) {}
