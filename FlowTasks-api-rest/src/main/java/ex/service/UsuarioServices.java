package ex.service;

import ex.model.Usuario;
import ex.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // Lógica de Login com conferência de Hash de senha
    public Usuario realizarLogin(String email, String senha) {
        Usuario usuario = repository.findByEmail(email);
        if (usuario != null && passwordEncoder.matches(senha, usuario.getSenha())) {
            return usuario;
        }
        return null;
    }

    // Busca o próprio perfil para o Dashboard do usuário comum
    public Usuario buscarMeuPerfil(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    // Atualização feita pelo próprio usuário (Nome, Email e Senha com criptografia)
    public Usuario atualizarProprioPerfil(Long id, Usuario dados) {
        Usuario alvo = repository.findById(id).orElseThrow();
        
        if (dados.getNome() != null) alvo.setNome(dados.getNome());
        if (dados.getEmail() != null) alvo.setEmail(dados.getEmail());
        
        if (dados.getSenha() != null && !dados.getSenha().isEmpty()) {
            alvo.setSenha(passwordEncoder.encode(dados.getSenha()));
        }
        
        return repository.save(alvo);
    }

    // --- REGRAS EXCLUSIVAS DE ADMINISTRADOR ---

    public List<Usuario> listarTodosParaAdmin(Long adminId) {
        validarAdmin(adminId);
        return repository.findAll();
    }

    public void deletarUsuarioPorAdmin(Long adminId, Long alvoId) {
        validarAdmin(adminId);
        repository.deleteById(alvoId);
    }

    public Usuario atualizarUsuarioPorAdmin(Long adminId, Long alvoId, Usuario dados) {
        validarAdmin(adminId);
        Usuario alvo = repository.findById(alvoId).orElseThrow();
        
        if (dados.getNome() != null) alvo.setNome(dados.getNome());
        if (dados.getEmail() != null) alvo.setEmail(dados.getEmail());
        
        if (dados.getSenha() != null && !dados.getSenha().isEmpty()) {
            alvo.setSenha(passwordEncoder.encode(dados.getSenha()));
        }

        // Permite que o Admin altere o perfil do usuário (Promoção a Admin)
        if (dados.getPerfil() != null) {
            alvo.setPerfil(dados.getPerfil());
        }
        
        return repository.save(alvo);
    }

    // Trava de segurança reutilizável
    private void validarAdmin(Long adminId) {
        Usuario admin = repository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin não encontrado."));
        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso Negado: Usuário não possui privilégios de Administrador.");
        }
    }
}