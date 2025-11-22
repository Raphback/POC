package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poc.model.Activite;
import poc.model.Lycee;
import poc.repository.ActiviteRepository;
import poc.repository.LyceeRepository;

import java.util.List;

@RestController
@RequestMapping("/api/referentiel")
@CrossOrigin(origins = "http://localhost:4200")
public class ReferenceController {

    @Autowired
    private ActiviteRepository activiteRepository;

    @Autowired
    private LyceeRepository lyceeRepository;

    @GetMapping("/activites")
    public List<Activite> getActivites() {
        return activiteRepository.findAll();
    }

    @GetMapping("/lycees")
    public List<Lycee> getLycees() {
        return lyceeRepository.findAll();
    }
}
