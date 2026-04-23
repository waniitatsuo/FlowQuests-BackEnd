package ex.controller;

import ex.model.Tarefa;
import ex.model.Usuario;
import ex.service.TarefaService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> criar(@RequestBody Tarefa novaTarefa) {
        try {
            Tarefa tarefaSalva = tarefaService.criarTarefa(novaTarefa);
            return ResponseEntity.status(HttpStatus.CREATED).body(tarefaSalva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<?> completarTarefa(@PathVariable Long id) {
        try {
            // O Service faz todo o trabalho pesado e devolve o usuário já com XP e conquistas atualizadas
            Usuario usuarioAtualizado = tarefaService.completarTarefa(id);
            
            // O @JsonManagedReference na entidade já cuida de não dar loop infinito!
            return ResponseEntity.ok(usuarioAtualizado);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Tarefa tarefaAtualizada) {
        try {
            Tarefa tarefa = tarefaService.atualizarTarefa(id, tarefaAtualizada);
            return ResponseEntity.ok(tarefa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (tarefaService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        tarefaService.deletarTarefa(id);
        return ResponseEntity.noContent().build();
    }
}