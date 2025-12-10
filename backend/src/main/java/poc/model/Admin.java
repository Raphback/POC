package poc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // In a real app, this should be hashed!

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ADMIN,
        SUPER_ADMIN
    }

    public Admin(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
