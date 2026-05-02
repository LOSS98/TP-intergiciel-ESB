package fr.insa.mesh.sample.service;

import fr.insa.mesh.sample.dto.SampleRequest;
import fr.insa.mesh.sample.model.Sample;
import fr.insa.mesh.sample.repository.SampleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SampleService {

    private final SampleRepository repository;

    public SampleService(SampleRepository repository) {
        this.repository = repository;
    }

    /** Enregistre un nouvel échantillon et lui attribue un identifiant unique. */
    public Sample register(SampleRequest request) {
        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Sample sample = new Sample(id, request.patientName(), request.examType(), request.sampleType());
        return repository.save(sample);
    }

    /** Retourne l'échantillon par son identifiant. */
    public Optional<Sample> findById(String id) {
        return repository.findById(id);
    }

    /** Liste tous les échantillons enregistrés. */
    public List<Sample> findAll() {
        return repository.findAll();
    }

    /** Met à jour le statut d'un échantillon (appelé par analysis-api). */
    public Optional<Sample> updateStatus(String id, Sample.Status newStatus) {
        return repository.findById(id).map(sample -> {
            sample.setStatus(newStatus);
            return repository.save(sample);
        });
    }
}
