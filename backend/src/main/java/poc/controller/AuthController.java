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
        Optional<Etudiant> etudiantOpt = etudiantRepository.findByMatriculeCsv(loginRequest.getMatricule());

        if (etudiantOpt.isEmpty()) {
            etudiantOpt = etudiantRepository.findByIne(loginRequest.getMatricule());
        }

        if (etudiantOpt.isPresent()) {
            Etudiant etudiant = etudiantOpt.get();
            String token = jwtUtils.generateToken(etudiant.getMatriculeCsv());
            return ResponseEntity.ok(new AuthResponse(token, etudiant));
        }

        return ResponseEntity.status(401).body("Identifiant incorrect (Matricule ou INE introuvable)");
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody poc.dto.AdminLoginRequest loginRequest) {
        Optional<poc.model.Admin> adminOpt = adminRepository.findByUsername(loginRequest.getUsername());

        if (adminOpt.isPresent()) {
            poc.model.Admin admin = adminOpt.get();
            if (admin.getPassword().equals(loginRequest.getPassword())) {
                String token = jwtUtils.generateToken(admin.getUsername());
                return ResponseEntity.ok(java.util.Map.of(
                        "token", token,
                        "role", admin.getRole(),
                        "username", admin.getUsername()));
            }
        }
        return ResponseEntity.status(401).body("Identifiants Admin incorrects");
    }

    @Autowired
    private poc.repository.ViewerRepository viewerRepository;

    @PostMapping("/login/viewer")
    public ResponseEntity<?> loginViewer(@RequestBody poc.dto.AdminLoginRequest loginRequest) {
        Optional<poc.model.Viewer> viewerOpt = viewerRepository.findByEmail(loginRequest.getUsername().toLowerCase());

        if (viewerOpt.isPresent()) {
            poc.model.Viewer viewer = viewerOpt.get();
            if (viewer.getPassword().equals(loginRequest.getPassword())) {
                String token = jwtUtils.generateToken(viewer.getEmail());
                return ResponseEntity.ok(java.util.Map.of(
                        "token", token,
                        "role", "VIEWER",
                        "email", viewer.getEmail(),
                        "nom", viewer.getNom(),
                        "prenom", viewer.getPrenom(),
                        "lyceeId", viewer.getLycee().getId(),
                        "lyceeName", viewer.getLycee().getNom()));
            }
        }
        return ResponseEntity.status(401).body("Identifiants Viewer incorrects");
    }
}
