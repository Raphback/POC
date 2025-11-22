package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poc.dto.AuthResponse;
import poc.dto.LoginRequest;
import poc.model.Etudiant;
import poc.repository.EtudiantRepository;
import poc.security.JwtUtils;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("🔍 Tentative de connexion : Matricule='" + loginRequest.getMatricule() + "', Nom='" + loginRequest.getNom() + "'");
        
        // Validation basique : Matricule + Nom (Case insensitive pour le nom)
        Optional<Etudiant> etudiantOpt = etudiantRepository.findByMatriculeCsv(loginRequest.getMatricule());

        if (etudiantOpt.isPresent()) {
            Etudiant etudiant = etudiantOpt.get();
            System.out.println("✅ Étudiant trouvé en base : Matricule='" + etudiant.getMatriculeCsv() + "', Nom='" + etudiant.getNom() + "'");
            
            if (etudiant.getNom().equalsIgnoreCase(loginRequest.getNom())) {
                // Authentification réussie
                System.out.println("🔓 Authentification réussie !");
                String token = jwtUtils.generateToken(etudiant.getMatriculeCsv());
                return ResponseEntity.ok(new AuthResponse(token, etudiant));
            } else {
                System.out.println("❌ Échec : Nom incorrect. Attendu='" + etudiant.getNom() + "', Reçu='" + loginRequest.getNom() + "'");
            }
        } else {
            System.out.println("❌ Échec : Matricule introuvable en base.");
            // Debug : lister tous les étudiants pour voir ce qu'il y a
            System.out.println("📋 Liste des étudiants en base :");
            etudiantRepository.findAll().forEach(e -> System.out.println(" - " + e.getMatriculeCsv() + " / " + e.getNom()));
        }

        return ResponseEntity.status(401).body("Identifiants incorrects (Matricule ou Nom invalide)");
    }
}
