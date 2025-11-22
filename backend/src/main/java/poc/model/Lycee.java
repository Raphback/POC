package poc.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Lycee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nom;
}
