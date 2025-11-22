package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poc.service.VoeuService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voeux")
@CrossOrigin(origins = "http://localhost:4200")
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
            System.out.println("🔍 Payload reçu : " + payload);
            
            // Extraction basique des données (à améliorer avec un DTO)
            Long etudiantId = Long.valueOf(payload.get("etudiantId").toString());
            List<?> rawIds = (List<?>) payload.get("activitesIds");
            
            System.out.println("📊 EtudiantId: " + etudiantId);
            System.out.println("📊 RawIds: " + rawIds);
            
            List<Long> activitesIds = rawIds.stream().map(id -> {
                if (id instanceof Number) {
                    return ((Number) id).longValue();
                } else {
                    return Long.parseLong(id.toString());
                }
            }).toList();
            
            System.out.println("✅ Activités IDs convertis: " + activitesIds);

            voeuService.enregistrerVoeux(etudiantId, activitesIds);
            System.out.println("✅ Vœux enregistrés avec succès pour l'étudiant " + etudiantId);
            return ResponseEntity.ok("Vœux enregistrés avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'enregistrement des vœux:");
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}
