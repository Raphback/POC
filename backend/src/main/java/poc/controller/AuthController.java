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

    @Autowired
    private poc.repository.AdminRepository adminRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("🔍 Tentative de connexion : Matricule='" + loginRequest.getMatricule() + "'");
        
        // Validation basique : Matricule uniquement
        Optional<Etudiant> etudiantOpt = etudiantRepository.findByMatriculeCsv(loginRequest.getMatricule());

        if (etudiantOpt.isPresent()) {
            Etudiant etudiant = etudiantOpt.get();
            System.out.println("✅ Étudiant trouvé en base : Matricule='" + etudiant.getMatriculeCsv() + "'");
            
            // Authentification réussie (sans vérification de nom)
            System.out.println("🔓 Authentification réussie !");
            String token = jwtUtils.generateToken(etudiant.getMatriculeCsv());
            return ResponseEntity.ok(new AuthResponse(token, etudiant));
        } else {
            System.out.println("❌ Échec : Matricule introuvable en base.");
            // Debug : lister tous les étudiants pour voir ce qu'il y a
            System.out.println("📋 Liste des étudiants en base :");
            etudiantRepository.findAll().forEach(e -> System.out.println(" - " + e.getMatriculeCsv()));
        }

        return ResponseEntity.status(401).body("Identifiant incorrect (Matricule introuvable)");
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody poc.dto.AdminLoginRequest loginRequest) {
        System.out.println("🔍 Tentative de connexion Admin : " + loginRequest.getUsername());

        Optional<poc.model.Admin> adminOpt = adminRepository.findByUsername(loginRequest.getUsername());

        if (adminOpt.isPresent()) {
            poc.model.Admin admin = adminOpt.get();
            if (admin.getPassword().equals(loginRequest.getPassword())) {
                System.out.println("🔓 Authentification Admin réussie !");
                // We might need a different token structure or just use the username as subject
                // For now, we reuse the same token generation but maybe prefix it or handle it in JwtUtils
                // But wait, JwtUtils might expect a matricule. Let's assume it just takes a string subject.
                String token = jwtUtils.generateToken(admin.getUsername());
                // We return the admin object wrapped in a response, or a specific AdminAuthResponse
                // For simplicity, we can reuse AuthResponse but we need to handle the 'etudiant' field being null or add 'admin' field
                // Let's create a generic response map or modify AuthResponse.
                // Actually AuthResponse expects an Etudiant. Let's make a new response or just return a Map.
                return ResponseEntity.ok(java.util.Map.of(
                    "token", token,
                    "role", admin.getRole(),
                    "username", admin.getUsername()
                ));
            }
        }
        return ResponseEntity.status(401).body("Identifiants Admin incorrects");
    }
}
