package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poc.model.*;
import poc.repository.*;
import poc.service.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private CsvImportService importService;
    @Autowired private AssignmentService assignmentService;
    @Autowired private PdfService pdfService;
    @Autowired private StatisticsService statisticsService;
    @Autowired private EtudiantRepository etudiantRepository;
    @Autowired private VoeuRepository voeuRepository;
    @Autowired private ActiviteRepository activiteRepository;
    @Autowired private LyceeRepository lyceeRepository;

    @PostMapping("/import")
    public ResponseEntity<String> importData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("Fichier vide");
        try {
            importService.importerEleves(file);
            return ResponseEntity.ok("Import reussi !");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur import : " + e.getMessage());
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<String> runAssignment() {
        try {
            return ResponseEntity.ok(assignmentService.runAssignment());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur affectation : " + e.getMessage());
        }
    }

    @GetMapping("/affectations")
    public ResponseEntity<List<Affectation>> getAffectations() {
        return ResponseEntity.ok(assignmentService.getAllAffectations());
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportPdf() {
        var bis = pdfService.generateTickets(assignmentService.getAllAffectations());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=convocations.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // Database CRUD
    @GetMapping("/etudiants")
    public List<Etudiant> getAllEtudiants() { return etudiantRepository.findAll(); }

    @GetMapping("/voeux")
    public List<Voeu> getAllVoeux() { return voeuRepository.findAll(); }

    @DeleteMapping("/etudiants/{id}")
    public ResponseEntity<String> deleteEtudiant(@PathVariable Long id) {
        return deleteEntity(() -> etudiantRepository.deleteById(id), "Etudiant");
    }

    @DeleteMapping("/activites/{id}")
    public ResponseEntity<String> deleteActivite(@PathVariable Long id) {
        return deleteEntity(() -> activiteRepository.deleteById(id), "Activite");
    }

    @DeleteMapping("/lycees/{id}")
    public ResponseEntity<String> deleteLycee(@PathVariable Long id) {
        return deleteEntity(() -> lyceeRepository.deleteById(id), "Lycee");
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() { return statisticsService.getGlobalStatistics(); }

    private ResponseEntity<String> deleteEntity(Runnable action, String name) {
        try {
            action.run();
            return ResponseEntity.ok(name + " supprime");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur suppression : " + e.getMessage());
        }
    }
}
