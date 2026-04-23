package ex.service;

import ex.model.Usuario;
import ex.model.Usuario.Perfil;
import ex.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Indica para o Spring que esta é a classe de regras de negócio
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 1. Caso de Uso: Se Registrar (Usuário Comum)
    public Usuario registrarUsuario(Usuario novoUsuario) {
        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));
        // O perfil e status já são USER e ATIVO por padrão na Entidade
        return repository.save(novoUsuario);
    }

    // 2. Caso de Uso: Criar conta de administrador
    public Usuario registrarAdministrador(Usuario novoAdmin) {
        novoAdmin.setSenha(passwordEncoder.encode(novoAdmin.getSenha()));
        novoAdmin.promoverParaAdmin(); // Usa o método encapsulado que criamos!
        return repository.save(novoAdmin);
    }

    // 3. Caso de Uso: Fazer Login
    public Usuario realizarLogin(String email, String senha) {
        Optional<Usuario> usuarioOptional = repository.findByEmail(email);

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            
            // Verifica a senha e se a conta NÃO está bloqueada ou banida
            if (passwordEncoder.matches(senha, usuario.getSenha()) && 
                usuario.getStatusConta() == Usuario.StatusConta.ATIVO) {
                return usuario;
            }
        }
        return null; // Retorna null se falhar na senha, e-mail ou se estiver bloqueado
    }

    // 4. Caso de Uso: Moderar Usuários (Bloquear)
    public Usuario bloquearUsuario(Long id) {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.bloquearConta();
        return repository.save(usuario);
    }

    // 5. Caso de Uso: Moderar Usuários (Banir)
    public Usuario banirUsuario(Long id) {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.banirConta();
        return repository.save(usuario);
    }

    // Métodos básicos de busca
    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id);
    }
    
    public void deletarUsuario(Long id) {
        repository.deleteById(id);
    }
}