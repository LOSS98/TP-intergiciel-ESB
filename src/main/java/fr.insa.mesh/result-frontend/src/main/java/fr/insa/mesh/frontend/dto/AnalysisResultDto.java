package fr.insa.mesh.frontend.dto;

import java.time.LocalDateTime;

public record AnalysisResultDto(
        String sampleId,
        String patientName,
        String examType,
        double value,
        String unit,
        String interpretation,
        LocalDateTime analyzedAt,
        boolean validated,
        String validatorSignature
) {}
