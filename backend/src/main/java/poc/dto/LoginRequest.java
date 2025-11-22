package poc.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String matricule; // INE ou Matricule Fauriel
    private String nom;
}
