package fr.insa.mesh.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/monservice")
public class DemoController {

    /**
     * GET /monservice/echo/{nom}
     * Retourne un echo du nom passé en paramètre.
     */
    @GetMapping("/echo/{nom}")
    public ResponseEntity<Map<String, String>> echo(@PathVariable String nom) {
        return ResponseEntity.ok(Map.of(
                "echo", nom,
                "message", "Vous avez dit : " + nom
        ));
    }

    /**
     * POST /monservice/hello
     * Body JSON : {"nom": "value"}
     * Retourne un message de salutation.
     */
    @PostMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(@RequestBody Map<String, String> body) {
        String nom = body.getOrDefault("nom", "inconnu");
        return ResponseEntity.ok(Map.of(
                "message", "Bonjour, " + nom + " !",
                "statut", "OK"
        ));
    }
}
