package fr.insa.mesh.frontend.controller;

import fr.insa.mesh.frontend.dto.AnalysisResultDto;
import fr.insa.mesh.frontend.dto.SampleDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class FrontendController {

    private final RestClient sampleApiClient;
    private final RestClient analysisApiClient;

    public FrontendController(
            @Qualifier("sampleApiClient") RestClient sampleApiClient,
            @Qualifier("analysisApiClient") RestClient analysisApiClient) {
        this.sampleApiClient = sampleApiClient;
        this.analysisApiClient = analysisApiClient;
    }

    /**
     * GET /
     * Page principale : liste tous les échantillons avec leurs résultats si disponibles.
     */
    @GetMapping("/")
    public String index(Model model) {
        List<SampleDto> samples = fetchAllSamples();

        // Pour chaque échantillon, on tente de récupérer le résultat d'analyse
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SampleDto s : samples) {
            AnalysisResultDto result = fetchResult(s.id());
            rows.add(Map.of("sample", s, "result", result != null ? result : ""));
        }

        model.addAttribute("rows", rows);
        return "index";
    }

    /**
     * POST /samples/create
     * Crée un nouvel échantillon via sample-api.
     */
    @PostMapping("/samples/create")
    public String createSample(
            @RequestParam String patientName,
            @RequestParam String examType,
            @RequestParam String sampleType,
            RedirectAttributes redirectAttributes) {
        try {
            SampleDto created = sampleApiClient.post()
                    .uri("/samples")
                    .body(Map.of(
                            "patientName", patientName,
                            "examType", examType,
                            "sampleType", sampleType
                    ))
                    .retrieve()
                    .body(SampleDto.class);
            redirectAttributes.addFlashAttribute("success",
                    "Échantillon enregistré : " + (created != null ? created.id() : ""));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur création : " + e.getMessage());
        }
        return "redirect:/";
    }

    /**
     * POST /samples/{id}/analyze
     * Déclenche l'analyse d'un échantillon via analysis-api.
     */
    @PostMapping("/samples/{id}/analyze")
    public String triggerAnalysis(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            AnalysisResultDto result = analysisApiClient.post()
                    .uri("/analyze/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new IllegalStateException("Échantillon introuvable");
                    })
                    .body(AnalysisResultDto.class);

            if (result != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Analyse terminée : " + result.examType()
                        + " = " + result.value() + " " + result.unit()
                        + " [" + result.interpretation() + "]");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur analyse : " + e.getMessage());
        }
        return "redirect:/";
    }

    // ── Méthodes privées ─────────────────────────────────────────────────────

    private List<SampleDto> fetchAllSamples() {
        try {
            List<SampleDto> result = sampleApiClient.get()
                    .uri("/samples")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result != null ? result : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private AnalysisResultDto fetchResult(String sampleId) {
        try {
            return analysisApiClient.get()
                    .uri("/results/{id}", sampleId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
                    .body(AnalysisResultDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}
