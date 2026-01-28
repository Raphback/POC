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
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/viewer")
public class ViewerController {

    @Autowired
    private EtudiantRepository etudiantRepository;
    @Autowired
    private VoeuRepository voeuRepository;

    @GetMapping("/etudiants/{lyceeId}")
    public ResponseEntity<List<Etudiant>> getEtudiantsByLycee(@PathVariable Long lyceeId) {
        return ResponseEntity.ok(etudiantRepository.findByLyceeId(lyceeId));
    }

    @GetMapping("/voeux/{lyceeId}")
    public ResponseEntity<List<Voeu>> getVoeuxByLycee(@PathVariable Long lyceeId) {
        return ResponseEntity.ok(voeuRepository.findByEtudiantLyceeId(lyceeId));
    }

    @GetMapping("/stats/{lyceeId}")
    public ResponseEntity<Map<String, Object>> getStatsByLycee(@PathVariable Long lyceeId) {
        List<Etudiant> etudiants = etudiantRepository.findByLyceeId(lyceeId);
        List<Voeu> voeux = voeuRepository.findByEtudiantLyceeId(lyceeId);

        Set<Long> etudiantsAvecVoeux = voeux.stream()
                .map(v -> v.getEtudiant().getId())
                .collect(Collectors.toSet());

        long avecVoeux = etudiantsAvecVoeux.size();
        int total = etudiants.size();

        return ResponseEntity.ok(Map.of(
                "totalStudents", total,
                "studentsWithVoeux", avecVoeux,
                "studentsWithoutVoeux", total - avecVoeux,
                "totalVoeux", voeux.size(),
                "participationRate", total > 0 ? (avecVoeux * 100.0 / total) : 0));
    }
}
