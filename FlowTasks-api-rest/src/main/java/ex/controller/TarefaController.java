package ex.controller;

import ex.model.Tarefa;
import ex.service.TarefaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tarefas")
@CrossOrigin("*")
public class TarefaController {

    @Autowired
    private TarefaService tarefaService;

    @PostMapping
    public ResponseEntity<?> criarTarefa(@RequestBody Tarefa tarefa, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(tarefaService.criarTarefa(tarefa, usuarioId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<?> completarTarefa(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            return ResponseEntity.ok(tarefaService.completarTarefa(id, usuarioId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<?> listarTarefas(
            @PathVariable Long userId,
            @RequestHeader("X-Usuario-Id") Long headerUserId,
            @RequestParam(defaultValue = "pendente") String estado,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!userId.equals(headerUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso Negado.");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Tarefa> tarefas = tarefaService.listarTarefasDoUsuario(userId, estado, search, pageable);
            
            // CORREÇÃO: .getContent() extrai apenas a lista (Array) de dentro da Página!
            return ResponseEntity.ok(tarefas.getContent()); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}