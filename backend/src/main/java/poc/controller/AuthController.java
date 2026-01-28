package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poc.dto.AuthResponse;
import poc.dto.LoginRequest;
import poc.dto.AdminLoginRequest;
import poc.model.Admin;
import poc.model.Etudiant;
import poc.model.Viewer;
import poc.repository.AdminRepository;
import poc.repository.EtudiantRepository;
import poc.repository.ViewerRepository;
import poc.security.JwtUtils;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private EtudiantRepository etudiantRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private ViewerRepository viewerRepository;
    @Autowired private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Etudiant> etudiant = etudiantRepository.findByMatriculeCsv(request.getMatricule());
        if (etudiant.isEmpty()) etudiant = etudiantRepository.findByIne(request.getMatricule());

        if (etudiant.isPresent()) {
            String token = jwtUtils.generateToken(etudiant.get().getMatriculeCsv());
            return ResponseEntity.ok(new AuthResponse(token, etudiant.get()));
        }
        return ResponseEntity.status(401).body("Identifiant incorrect");
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody AdminLoginRequest request) {
        Optional<Admin> admin = adminRepository.findByUsername(request.getUsername());
        if (admin.isPresent() && admin.get().getPassword().equals(request.getPassword())) {
            String token = jwtUtils.generateToken(admin.get().getUsername());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", admin.get().getRole(),
                    "username", admin.get().getUsername()));
        }
        return ResponseEntity.status(401).body("Identifiants Admin incorrects");
    }

    @PostMapping("/login/viewer")
    public ResponseEntity<?> loginViewer(@RequestBody AdminLoginRequest request) {
        Optional<Viewer> viewer = viewerRepository.findByEmail(request.getUsername().toLowerCase());
        if (viewer.isPresent() && viewer.get().getPassword().equals(request.getPassword())) {
            Viewer v = viewer.get();
            String token = jwtUtils.generateToken(v.getEmail());
            return ResponseEntity.ok(Map.of(
                    "token", token, "role", "VIEWER",
                    "email", v.getEmail(), "nom", v.getNom(), "prenom", v.getPrenom(),
                    "lyceeId", v.getLycee().getId(), "lyceeName", v.getLycee().getNom()));
        }
        return ResponseEntity.status(401).body("Identifiants Viewer incorrects");
    }
}
