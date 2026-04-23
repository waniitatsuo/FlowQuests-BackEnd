package ex.controller;

import ex.model.Conquista;
import ex.service.ConquistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/conquistas")
@CrossOrigin("*")
public class ConquistaController {

    @Autowired
    private ConquistaService conquistaService;

    // Endpoint: Listar todas as conquistas (Usado pelo seu dashboard)
    @GetMapping
    public ResponseEntity<List<Conquista>> listarTudo(@RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId) {
        // Se passarmos o ID, podemos aproveitar para rodar a verificação antes de mostrar
        if (usuarioId != null) {
            conquistaService.verificarEDesignarConquistas(usuarioId);
        }
        
        List<Conquista> lista = conquistaService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    // Endpoint: Criar nova conquista (Protegido por lógica de Admin se desejar)
    @PostMapping
    public ResponseEntity<Conquista> salvar(@RequestBody Conquista conquista) {
        return ResponseEntity.ok(conquistaService.criarConquista(conquista));
    }
}