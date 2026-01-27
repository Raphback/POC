package poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poc.model.Viewer;

import java.util.Optional;

public interface ViewerRepository extends JpaRepository<Viewer, Long> {
    Optional<Viewer> findByEmail(String email);

    boolean existsByEmail(String email);
}
