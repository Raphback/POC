package poc.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "voeu")
public class Voeu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "activite_id")
    private Activite activite;

    // De 1 à 5
    private Integer priorite;
}
