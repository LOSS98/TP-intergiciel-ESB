package fr.insa.mesh.sample.controller;

import fr.insa.mesh.sample.dto.SampleRequest;
import fr.insa.mesh.sample.model.Sample;
import fr.insa.mesh.sample.service.SampleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/samples")
public class SampleController {

    private final SampleService service;

    public SampleController(SampleService service) {
        this.service = service;
    }

    /**
     * POST /samples
     * Enregistre un nouvel échantillon.
     * Body : {"patientName":"Jean Dupont","examType":"Glycémie","sampleType":"Sang"}
     */
    @PostMapping
    public ResponseEntity<Sample> createSample(@RequestBody SampleRequest request) {
        Sample sample = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sample);
    }

    /**
     * GET /samples/{id}
     * Retourne l'état actuel d'un échantillon.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Sample> getSample(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /samples
     * Liste tous les échantillons (utilisé par result-frontend).
     */
    @GetMapping
    public ResponseEntity<List<Sample>> getAllSamples() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * PATCH /samples/{id}/status
     * Met à jour le statut d'un échantillon (appelé par analysis-api).
     * Body : {"status":"IN_ANALYSIS"}
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Sample> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            Sample.Status newStatus = Sample.Status.valueOf(body.get("status"));
            return service.updateStatus(id, newStatus)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
