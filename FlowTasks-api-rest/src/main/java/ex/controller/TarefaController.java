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

    // Endpoint: Criar Tarefa
    @PostMapping
    public ResponseEntity<?> criarTarefa(
            @RequestBody Tarefa tarefa, 
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            Tarefa novaTarefa = tarefaService.criarTarefa(tarefa, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaTarefa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint: Completar a Tarefa (O "Check" do Dashboard)
    @PostMapping("/{id}/completar")
    public ResponseEntity<?> completarTarefa(
            @PathVariable Long id, 
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            Tarefa tarefaCompletada = tarefaService.completarTarefa(id, usuarioId);
            return ResponseEntity.ok(tarefaCompletada);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint: Listar Tarefas (Com Paginação e Busca)
    @GetMapping("/usuario/{userId}")
    public ResponseEntity<?> listarTarefas(
            @PathVariable Long userId,
            @RequestHeader("X-Usuario-Id") Long headerUserId,
            @RequestParam(defaultValue = "pendente") String estado,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        // Barreira de segurança: O cara logado no header só pode ver a rota do próprio ID
        if (!userId.equals(headerUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso Negado: Você só pode ver suas próprias tarefas.");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Tarefa> tarefas = tarefaService.listarTarefasDoUsuario(userId, estado, search, pageable);
            return ResponseEntity.ok(tarefas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}