package fr.insa.mesh.sample.dto;

public record SampleRequest(
        String patientName,
        String examType,
        String sampleType
) {}
