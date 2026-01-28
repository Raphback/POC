package poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import poc.model.Activite;
import poc.model.Lycee;
import poc.repository.ActiviteRepository;
import poc.repository.LyceeRepository;

import java.util.List;

@RestController
@RequestMapping("/api/referentiel")
public class ReferenceController {

    @Autowired private ActiviteRepository activiteRepository;
    @Autowired private LyceeRepository lyceeRepository;

    @GetMapping("/activites")
    public List<Activite> getActivites() { return activiteRepository.findAll(); }

    @GetMapping("/lycees")
    public List<Lycee> getLycees() { return lyceeRepository.findAll(); }
}
