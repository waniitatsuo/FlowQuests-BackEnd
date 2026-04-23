package ex.service;

import ex.model.Tarefa;
import ex.model.Usuario;
import ex.repository.TarefaRepository;
import ex.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    public TarefaService(TarefaRepository tarefaRepository, UsuarioRepository usuarioRepository) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Criar nova tarefa
    public Tarefa criarTarefa(Tarefa tarefa, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        tarefa.setUsuario(usuario);
        tarefa.setConcluida(false); // Nasce pendente por padrão
        
        return tarefaRepository.save(tarefa);
    }

    // Listar as tarefas do Dashboard (Com paginação)
    public Page<Tarefa> listarTarefasDoUsuario(Long usuarioId, String estado, String search, Pageable pageable) {
        // Se o estado for "concluida", vira true. Se for "pendente", vira false.
        boolean isConcluida = estado != null && estado.equalsIgnoreCase("concluida");
        
        return tarefaRepository.findByUsuarioIdAndConcluidaAndTituloContainingIgnoreCase(
                usuarioId, isConcluida, search, pageable);
    }

    // Completar a tarefa e dar o XP pro jogador
    public Tarefa completarTarefa(Long tarefaId, Long usuarioId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada."));

        // Proteção: O cara só pode completar a própria tarefa!
        if (!tarefa.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("Acesso Negado: Essa tarefa pertence a outro jogador.");
        }

        // Só dá o XP se ela ainda não estiver concluída
        if (!tarefa.isConcluida()) {
            tarefa.setConcluida(true);
            
            Usuario usuario = tarefa.getUsuario();
            usuario.setXpTotal(usuario.getXpTotal() + tarefa.getXpRecompensa());
            usuarioRepository.save(usuario); // Salva o novo XP do cara
        }
        
        return tarefaRepository.save(tarefa);
    }
}