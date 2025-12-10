package poc.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "activite")
public class Activite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Enumerated(EnumType.STRING)
    private TypeActivite type;

    private Integer nbPlaces; // Capacity (Jauge)

    private String salle;
}
