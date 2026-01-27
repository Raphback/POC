package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poc.model.Etudiant;
import poc.model.Voeu;
import poc.repository.EtudiantRepository;
import poc.repository.VoeuRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/viewer")
@CrossOrigin(origins = "http://localhost:4200")
public class ViewerController {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private VoeuRepository voeuRepository;

    /**
     * Get all students for a specific lycee (for viewer access)
     */
    @GetMapping("/etudiants/{lyceeId}")
    public ResponseEntity<List<Etudiant>> getEtudiantsByLycee(@PathVariable Long lyceeId) {
        List<Etudiant> etudiants = etudiantRepository.findAll().stream()
                .filter(e -> e.getLycee() != null && e.getLycee().getId().equals(lyceeId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(etudiants);
    }

    /**
     * Get all voeux for students of a specific lycee
     */
    @GetMapping("/voeux/{lyceeId}")
    public ResponseEntity<List<Voeu>> getVoeuxByLycee(@PathVariable Long lyceeId) {
        List<Voeu> voeux = voeuRepository.findAll().stream()
                .filter(v -> v.getEtudiant() != null
                        && v.getEtudiant().getLycee() != null
                        && v.getEtudiant().getLycee().getId().equals(lyceeId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(voeux);
    }

    /**
     * Get statistics for a specific lycee
     */
    @GetMapping("/stats/{lyceeId}")
    public ResponseEntity<Map<String, Object>> getStatsByLycee(@PathVariable Long lyceeId) {
        List<Etudiant> etudiants = etudiantRepository.findAll().stream()
                .filter(e -> e.getLycee() != null && e.getLycee().getId().equals(lyceeId))
                .collect(Collectors.toList());

        List<Voeu> voeux = voeuRepository.findAll().stream()
                .filter(v -> v.getEtudiant() != null
                        && v.getEtudiant().getLycee() != null
                        && v.getEtudiant().getLycee().getId().equals(lyceeId))
                .collect(Collectors.toList());

        long studentsWithVoeux = etudiants.stream()
                .filter(e -> voeux.stream().anyMatch(v -> v.getEtudiant().getId().equals(e.getId())))
                .count();

        return ResponseEntity.ok(Map.of(
                "totalStudents", etudiants.size(),
                "studentsWithVoeux", studentsWithVoeux,
                "studentsWithoutVoeux", etudiants.size() - studentsWithVoeux,
                "totalVoeux", voeux.size(),
                "participationRate", etudiants.isEmpty() ? 0 : (studentsWithVoeux * 100.0 / etudiants.size())));
    }
}
