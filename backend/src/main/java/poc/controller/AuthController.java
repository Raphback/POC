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
            System.out.println("✅ Admin trouvé en base : " + admin.getUsername());
            if (admin.getPassword().equals(loginRequest.getPassword())) {
                System.out.println("🔓 Authentification Admin réussie !");
                String token = jwtUtils.generateToken(admin.getUsername());
                return ResponseEntity.ok(java.util.Map.of(
                    "token", token,
                    "role", admin.getRole(),
                    "username", admin.getUsername()
                ));
            } else {
                System.out.println("❌ Mot de passe incorrect pour admin : " + admin.getUsername());
            }
        } else {
            System.out.println("❌ Admin introuvable en base pour username : " + loginRequest.getUsername());
            // Debug : lister tous les admins
            System.out.println("📋 Liste des admins en base :");
            adminRepository.findAll().forEach(a -> System.out.println(" - " + a.getUsername()));
        }
        return ResponseEntity.status(401).body("Identifiants Admin incorrects");
    }
}
