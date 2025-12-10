package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poc.model.Etudiant;
import poc.repository.EtudiantRepository;
import poc.repository.VoeuRepository;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "http://localhost:4200")
public class StatsController {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private VoeuRepository voeuRepository;

    @GetMapping("/global")
    public ResponseEntity<?> getGlobalStats() {
        long total = etudiantRepository.count();
        List<Long> studentsWithWishes = voeuRepository.findEtudiantsWithVoeux();
        long filled = studentsWithWishes.size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("filled", filled);
        stats.put("percent", total > 0 ? (double) filled / total * 100 : 0);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/lycee")
    public ResponseEntity<?> getLyceeStats() {
        List<Etudiant> allStudents = etudiantRepository.findAll();
        List<Long> studentsWithWishes = voeuRepository.findEtudiantsWithVoeux();
        Set<Long> filledIds = new HashSet<>(studentsWithWishes);

        Map<String, Map<String, Object>> statsByLycee = new HashMap<>();

        for (Etudiant e : allStudents) {
            String lyceeName = e.getLycee() != null ? e.getLycee().getNom() : "Inconnu";
            
            statsByLycee.putIfAbsent(lyceeName, new HashMap<>(Map.of("total", 0L, "filled", 0L)));
            Map<String, Object> stat = statsByLycee.get(lyceeName);
            
            stat.put("total", (long) stat.get("total") + 1);
            if (filledIds.contains(e.getId())) {
                stat.put("filled", (long) stat.get("filled") + 1);
            }
        }

        // Calculate percents
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : statsByLycee.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            long total = (long) stat.get("total");
            long filled = (long) stat.get("filled");
            stat.put("percent", total > 0 ? (double) filled / total * 100 : 0);
            stat.put("lycee", entry.getKey());
            result.add(stat);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/classe")
    public ResponseEntity<?> getClasseStats() {
        List<Etudiant> allStudents = etudiantRepository.findAll();
        List<Long> studentsWithWishes = voeuRepository.findEtudiantsWithVoeux();
        Set<Long> filledIds = new HashSet<>(studentsWithWishes);

        // Key: "LyceeName - ClasseName"
        Map<String, Map<String, Object>> statsByClasse = new HashMap<>();

        for (Etudiant e : allStudents) {
            String lyceeName = e.getLycee() != null ? e.getLycee().getNom() : "Inconnu";
            String classeName = e.getClasse() != null ? e.getClasse() : "Inconnue";
            String key = lyceeName + " - " + classeName;
            
            statsByClasse.putIfAbsent(key, new HashMap<>(Map.of("total", 0L, "filled", 0L, "lycee", lyceeName, "classe", classeName)));
            Map<String, Object> stat = statsByClasse.get(key);
            
            stat.put("total", (long) stat.get("total") + 1);
            if (filledIds.contains(e.getId())) {
                stat.put("filled", (long) stat.get("filled") + 1);
            }
        }

        // Calculate percents
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : statsByClasse.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            long total = (long) stat.get("total");
            long filled = (long) stat.get("filled");
            stat.put("percent", total > 0 ? (double) filled / total * 100 : 0);
            result.add(stat);
        }
        
        // Sort by Lycee then Classe
        result.sort((a, b) -> {
            int lyceeCmp = ((String) a.get("lycee")).compareTo((String) b.get("lycee"));
            if (lyceeCmp != 0) return lyceeCmp;
            return ((String) a.get("classe")).compareTo((String) b.get("classe"));
        });

        return ResponseEntity.ok(result);
    }

    @Autowired
    private poc.service.ExcelService excelService;

    @Autowired
    private poc.repository.ActiviteRepository activiteRepository;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportWishes() {
        List<poc.model.Activite> activites = activiteRepository.findAll();
        List<poc.model.Voeu> voeux = voeuRepository.findAll();
        
        byte[] content = excelService.generateWishesExport(activites, voeux);
        
        if (content == null) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=voeux_par_activite.xlsx")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(content);
    }
}
