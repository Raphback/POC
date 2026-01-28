package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poc.service.VoeuService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voeux")
public class VoeuController {

    @Autowired
    private VoeuService voeuService;
    @Autowired
    private poc.repository.EtudiantRepository etudiantRepository;

    @GetMapping("/etudiant/{matricule}")
    public ResponseEntity<?> getEtudiant(@PathVariable String matricule) {
        return etudiantRepository.findByMatriculeCsv(matricule)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(produces = "text/plain")
    public ResponseEntity<String> enregistrerVoeux(@RequestBody Map<String, Object> payload) {
        try {
            Long etudiantId = Long.valueOf(payload.get("etudiantId").toString());
            List<?> rawIds = (List<?>) payload.get("activitesIds");
            List<Long> activitesIds = rawIds.stream()
                    .map(id -> id instanceof Number ? ((Number) id).longValue() : Long.parseLong(id.toString()))
                    .toList();

            voeuService.enregistrerVoeux(etudiantId, activitesIds);
            return ResponseEntity.ok("Voeux enregistres avec succes !");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}
