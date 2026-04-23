package ex.controller;

import ex.model.Conquista;
import ex.model.repository.ConquistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conquistas")
@CrossOrigin("*")
public class ConquistaController {

    @Autowired
    private ConquistaRepository repository;
    
    // Endpoint para listar todas as conquistas possíveis
    @GetMapping
    public List<Conquista> listar() {
        return repository.findAll();
    }
}