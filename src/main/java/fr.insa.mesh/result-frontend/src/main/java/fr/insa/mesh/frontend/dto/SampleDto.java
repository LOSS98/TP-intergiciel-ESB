package fr.insa.mesh.frontend.dto;

import java.time.LocalDateTime;

public record SampleDto(
        String id,
        String patientName,
        String examType,
        String sampleType,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
