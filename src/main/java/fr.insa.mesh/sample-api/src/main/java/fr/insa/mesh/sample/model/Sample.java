package fr.insa.mesh.sample.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "samples")
public class Sample {

    public enum Status {
        REGISTERED, PRE_ANALYSIS, IN_ANALYSIS, VALIDATED, COMPLETED, REJECTED
    }

    @Id
    private String id;

    @Column(nullable = false)
    private String patientName;

    /** Type d'examen demandé : ex. "Glycémie", "NFS", "Créatinine" */
    @Column(nullable = false)
    private String examType;

    /** Type de prélèvement : "Sang", "Urine", "Biopsie" */
    @Column(nullable = false)
    private String sampleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ── Constructeurs ────────────────────────────────────────────────────────

    protected Sample() {}

    public Sample(String id, String patientName, String examType, String sampleType) {
        this.id = id;
        this.patientName = patientName;
        this.examType = examType;
        this.sampleType = sampleType;
        this.status = Status.REGISTERED;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getId() { return id; }

    public String getPatientName() { return patientName; }

    public String getExamType() { return examType; }

    public String getSampleType() { return sampleType; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
