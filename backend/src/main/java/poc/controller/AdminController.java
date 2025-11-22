package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poc.service.CsvImportService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private poc.service.AssignmentService assignmentService;

    @Autowired
    private poc.service.PdfService pdfService;

    @PostMapping("/import")
    public ResponseEntity<String> importData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier vide");
        }

        try {
            csvImportService.importerEleves(file);
            return ResponseEntity.ok("Import réussi !");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors de l'import : " + e.getMessage());
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<String> runAssignment() {
        try {
            String result = assignmentService.runAssignment();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erreur lors de l'affectation : " + e.getMessage());
        }
    }

    @GetMapping("/affectations")
    public ResponseEntity<java.util.List<poc.model.Affectation>> getAffectations() {
        return ResponseEntity.ok(assignmentService.getAllAffectations());
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportPdf() {
        java.io.ByteArrayInputStream bis = pdfService.generateTickets(assignmentService.getAllAffectations());

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=convocations.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(new org.springframework.core.io.InputStreamResource(bis));
    }

    // Database Admin Endpoints
    @Autowired
    private poc.repository.EtudiantRepository etudiantRepository;

    @Autowired
    private poc.repository.VoeuRepository voeuRepository;

    @Autowired
    private poc.repository.ActiviteRepository activiteRepository;

    @Autowired
    private poc.repository.LyceeRepository lyceeRepository;

    @GetMapping("/etudiants")
    public ResponseEntity<java.util.List<poc.model.Etudiant>> getAllEtudiants() {
        return ResponseEntity.ok(etudiantRepository.findAll());
    }

    @DeleteMapping("/etudiants/{id}")
    public ResponseEntity<String> deleteEtudiant(@PathVariable Long id) {
        try {
            etudiantRepository.deleteById(id);
            return ResponseEntity.ok("Étudiant supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @GetMapping("/voeux")
    public ResponseEntity<java.util.List<poc.model.Voeu>> getAllVoeux() {
        return ResponseEntity.ok(voeuRepository.findAll());
    }

    @DeleteMapping("/activites/{id}")
    public ResponseEntity<String> deleteActivite(@PathVariable Long id) {
        try {
            activiteRepository.deleteById(id);
            return ResponseEntity.ok("Activité supprimée avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @DeleteMapping("/lycees/{id}")
    public ResponseEntity<String> deleteLycee(@PathVariable Long id) {
        try {
            lyceeRepository.deleteById(id);
            return ResponseEntity.ok("Lycée supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Autowired
    private poc.service.StatisticsService statisticsService;

    @GetMapping("/statistics")
    public ResponseEntity<java.util.Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(statisticsService.getGlobalStatistics());
    }

    @GetMapping("/statistics/lycee/{id}")
    public ResponseEntity<java.util.Map<String, Object>> getStatisticsByLycee(@PathVariable Long id) {
        return ResponseEntity.ok(statisticsService.getStatisticsByLycee(id));
    }
}
