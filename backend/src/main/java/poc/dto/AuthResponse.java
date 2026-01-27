package poc.dto;

import poc.model.Etudiant;

public class AuthResponse {
    private String token;
    private Etudiant etudiant;

    public AuthResponse(String token, Etudiant etudiant) {
        this.token = token;
        this.etudiant = etudiant;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }
}
