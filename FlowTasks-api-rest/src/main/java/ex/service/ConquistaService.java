package ex.service;

import ex.model.Conquista;
import ex.model.Usuario;
import ex.repository.ConquistaRepository;
import ex.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConquistaService {

    private final ConquistaRepository conquistaRepository;
    private final UsuarioRepository usuarioRepository;

    public ConquistaService(ConquistaRepository conquistaRepository, UsuarioRepository usuarioRepository) {
        this.conquistaRepository = conquistaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Listar todas as conquistas do sistema para a vitrine
    public List<Conquista> listarTodas() {
        return conquistaRepository.findAll();
    }

    // Criar nova conquista (Ação de Admin)
    public Conquista criarConquista(Conquista conquista) {
        return conquistaRepository.save(conquista);
    }

    // Lógica principal: Verifica o XP do usuário e adiciona as conquistas que ele "bateu" a meta
    public void verificarEDesignarConquistas(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // 1. Busca todas as conquistas que o XP dele permite ter
        List<Conquista> elegiveis = conquistaRepository.findByXpNecessarioLessThanEqual(usuario.getXpTotal());

        // 2. Filtra apenas as que ele AINDA não possui na lista dele
        List<Conquista> novas = elegiveis.stream()
                .filter(c -> !usuario.getConquistas().contains(c))
                .collect(Collectors.toList());

        // 3. Se houver novidades, adiciona e salva
        if (!novas.isEmpty()) {
            usuario.getConquistas().addAll(novas);
            usuarioRepository.save(usuario);
        }
    }
}