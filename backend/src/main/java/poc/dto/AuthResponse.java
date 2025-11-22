package poc.dto;

import lombok.Data;
import poc.model.Etudiant;

@Data
public class AuthResponse {
    private String token;
    private Etudiant etudiant;

    public AuthResponse(String token, Etudiant etudiant) {
        this.token = token;
        this.etudiant = etudiant;
    }
}
