package ex.service;

import ex.model.Advertencia;
import ex.model.Usuario;
import ex.repository.AdvertenciaRepository;
import ex.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdvertenciaService {

    @Autowired
    private AdvertenciaRepository advertenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Advertencia aplicarAdvertencia(Long adminId, Long usuarioId, Advertencia novaAdvertencia) {
        
        // 1. VERIFICAÇÃO DE SEGURANÇA: Quem está tentando dar a advertência?
        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));

        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            // Se não for ADMIN, a gente bloqueia a ação na hora!
            throw new SecurityException("Acesso Negado. Apenas administradores podem aplicar advertências.");
        }

        // 2. Busca o usuário que vai sofrer a punição
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário a ser punido não encontrado."));

        // 3. Associa a advertência a ele e salva
        novaAdvertencia.setUsuario(usuario);
        Advertencia advertenciaSalva = advertenciaRepository.save(novaAdvertencia);

        // 4. REGRA DE NEGÓCIO: Se acumular 3 advertências, bloqueia a conta!
        long totalAdvertencias = advertenciaRepository.countByUsuarioId(usuario.getId());
        
        if (totalAdvertencias >= 3) {
            usuario.bloquearConta(); 
            usuarioRepository.save(usuario);
        }

        return advertenciaSalva;
    }
}
