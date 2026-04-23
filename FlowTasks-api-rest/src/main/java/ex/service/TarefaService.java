package ex.service;

import ex.model.Tarefa;
import ex.model.Usuario;
import ex.repository.ConquistaRepository;
import ex.repository.TarefaRepository;
import ex.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TarefaService {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConquistaRepository conquistaRepository;

    // 1. Criar Tarefa
    public Tarefa criarTarefa(Tarefa novaTarefa) {
        if (novaTarefa.getUsuario() == null || novaTarefa.getUsuario().getId() == null) {
            throw new IllegalArgumentException("O ID do usuário é obrigatório.");
        }

        Usuario usuario = usuarioRepository.findById(novaTarefa.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        novaTarefa.setUsuario(usuario);
        novaTarefa.calcularRecompensaXp(); // Usa a regra de negócio da Entidade
        
        return tarefaRepository.save(novaTarefa);
    }

    // 2. Marcar como Concluída (A lógica mais complexa fica aqui)
    @Transactional
    public Usuario completarTarefa(Long idTarefa) {
        Tarefa tarefa = tarefaRepository.findById(idTarefa)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));

        if (tarefa.getEstado() == Tarefa.Estado.concluida) {
            throw new IllegalStateException("A tarefa já está concluída");
        }

        Usuario usuario = tarefa.getUsuario();
        
        // Aplica as regras de negócio usando os métodos blindados
        tarefa.marcarComoConcluida();
        usuario.adicionarXp(tarefa.getRecompensaXp()); // O Controller antigo usava o setXpTotal()
        
        tarefaRepository.save(tarefa);

        // Lógica de Conquistas
        long tarefasConcluidas = tarefaRepository.countByUsuarioIdAndEstado(usuario.getId(), Tarefa.Estado.concluida);

        if (tarefasConcluidas == 1) {
            conquistaRepository.findById(1L).ifPresent(conquista -> usuario.getConquistas().add(conquista));
        }

        if (tarefasConcluidas == 10) {
            conquistaRepository.findById(2L).ifPresent(conquista -> usuario.getConquistas().add(conquista));
        }

        return usuarioRepository.save(usuario);
    }

    // Listagens e Buscas
    public List<Tarefa> listarTodas() {
        return tarefaRepository.findAll();
    }

    public Page<Tarefa> listarPorUsuarioPaginado(Long usuarioId, Tarefa.Estado estado, String search, Pageable paging) {
        return tarefaRepository.findByUsuarioIdAndEstadoAndTituloContainingIgnoreCase(usuarioId, estado, search, paging);
    }
    
    public Optional<Tarefa> buscarPorId(Long id) {
    	return tarefaRepository.findById(id);
    }

    // Atualizar e Deletar
    public Tarefa atualizarTarefa(Long id, Tarefa dadosAtualizados) {
        Tarefa tarefaExistente = tarefaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        
        tarefaExistente.setTitulo(dadosAtualizados.getTitulo());
        tarefaExistente.setDataPrazo(dadosAtualizados.getDataPrazo());
        tarefaExistente.setHoraPrazo(dadosAtualizados.getHoraPrazo());
        tarefaExistente.setCategoria(dadosAtualizados.getCategoria());
        
        // Se a categoria mudou, recalculamos o XP!
        tarefaExistente.calcularRecompensaXp();

        return tarefaRepository.save(tarefaExistente);
    }

    public void deletarTarefa(Long id) {
        tarefaRepository.deleteById(id);
    }
}