package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poc.model.Etudiant;
import poc.repository.ActiviteRepository;
import poc.repository.EtudiantRepository;
import poc.repository.VoeuRepository;
import poc.service.ExcelService;

import java.util.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired private EtudiantRepository etudiantRepository;
    @Autowired private VoeuRepository voeuRepository;
    @Autowired private ActiviteRepository activiteRepository;
    @Autowired private ExcelService excelService;

    @GetMapping("/global")
    public ResponseEntity<?> getGlobalStats() {
        long total = etudiantRepository.count();
        long filled = voeuRepository.findEtudiantsWithVoeux().size();
        return ResponseEntity.ok(Map.of(
                "total", total, "filled", filled,
                "percent", total > 0 ? (double) filled / total * 100 : 0));
    }

    @GetMapping("/lycee")
    public ResponseEntity<?> getLyceeStats() {
        return ResponseEntity.ok(buildStats(false));
    }

    @GetMapping("/classe")
    public ResponseEntity<?> getClasseStats() {
        List<Map<String, Object>> result = buildStats(true);
        result.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("lycee"))
                .thenComparing(m -> (String) m.getOrDefault("classe", "")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportWishes() {
        byte[] content = excelService.generateWishesExport(
                activiteRepository.findAll(), voeuRepository.findAll());
        if (content == null) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=voeux_par_activite.xlsx")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(content);
    }

    private List<Map<String, Object>> buildStats(boolean byClasse) {
        List<Etudiant> all = etudiantRepository.findAll();
        Set<Long> filledIds = new HashSet<>(voeuRepository.findEtudiantsWithVoeux());

        Map<String, Map<String, Object>> grouped = new HashMap<>();
        for (Etudiant e : all) {
            String lycee = e.getLycee() != null ? e.getLycee().getNom() : "Inconnu";
            String key = byClasse ? lycee + " - " + (e.getClasse() != null ? e.getClasse() : "Inconnue") : lycee;

            grouped.putIfAbsent(key, new HashMap<>(Map.of("total", 0L, "filled", 0L, "lycee", lycee)));
            if (byClasse) grouped.get(key).put("classe", e.getClasse() != null ? e.getClasse() : "Inconnue");

            Map<String, Object> stat = grouped.get(key);
            stat.put("total", (long) stat.get("total") + 1);
            if (filledIds.contains(e.getId())) stat.put("filled", (long) stat.get("filled") + 1);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            Map<String, Object> stat = entry.getValue();
            long total = (long) stat.get("total");
            long filled = (long) stat.get("filled");
            stat.put("percent", total > 0 ? (double) filled / total * 100 : 0);
            result.add(stat);
        }
        return result;
    }
}
