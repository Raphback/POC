package poc.model;

import jakarta.persistence.*;

@Entity
public class Viewer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nom;
    private String prenom;

    @ManyToOne
    @JoinColumn(name = "lycee_id")
    private Lycee lycee;

    public Viewer() {
    }

    public Viewer(String email, String password, String nom, String prenom, Lycee lycee) {
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.lycee = lycee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Lycee getLycee() {
        return lycee;
    }

    public void setLycee(Lycee lycee) {
        this.lycee = lycee;
    }
}
