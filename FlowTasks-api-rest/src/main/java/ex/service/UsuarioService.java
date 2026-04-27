package ex.service;

import ex.model.Usuario;
import ex.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Indica para o Spring que esta é a classe de regras de negócio
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    // Construtor com as injeções
    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Caso de Uso: Se Registrar (Usuário Comum)
    public Usuario registrarUsuario(Usuario novoUsuario) {
        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));
        // O perfil e status já são USER e ATIVO por padrão na Entidade
        return repository.save(novoUsuario);
    }

    // 2. Caso de Uso: Criar conta de administrador
    public Usuario registrarAdministrador(Long adminLogadoId, Usuario novoAdmin) {
        
        // 1. Verifica se quem está tentando criar a conta realmente é um ADMIN
        Usuario adminLogado = repository.findById(adminLogadoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));

        if (adminLogado.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso negado. Você não tem permissão para realizar esta operação.");
        }

        // 2. Se for admin, a gente prossegue com a criação do novo colega!
        novoAdmin.setSenha(passwordEncoder.encode(novoAdmin.getSenha()));
        novoAdmin.promoverParaAdmin(); 
        
        return repository.save(novoAdmin);
    }

    // Caso de Uso: Promover um usuário existente para ADMINISTRADOR
    public Usuario promoverUsuario(Long adminLogadoId, Long usuarioAlvoId) {

        // 1. Reutilizamos sua lógica de segurança: Verifica se quem solicita é ADMIN
        Usuario adminLogado = repository.findById(adminLogadoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));

        if (adminLogado.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso negado. Apenas administradores podem promover outros usuários.");
        }

        // 2. Busca o usuário que será promovido
        Usuario usuarioAlvo = repository.findById(usuarioAlvoId)
                .orElseThrow(() -> new IllegalArgumentException("Aventureiro alvo não encontrado."));

        // 3. Aplica a promoção (usando o seu método interno do modelo)
        usuarioAlvo.setPerfil(Usuario.Perfil.ADMIN);
        // Se o seu método no modelo for exatamente 'promoverParaAdmin()', use:
        // usuarioAlvo.promoverParaAdmin();

        // 4. Salva a alteração no banco
        return repository.save(usuarioAlvo);
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

    // 4. Caso de Uso: Moderar Usuários (Bloquear Direto)
    public Usuario bloquearUsuario(Long adminId, Long id) {
        // Verifica se quem pediu é admin
        Usuario admin = repository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));
        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso Negado. Apenas administradores podem bloquear contas.");
        }

        Usuario usuario = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        usuario.bloquearConta();
        return repository.save(usuario);
    }

    // 5. Caso de Uso: Moderar Usuários (Banir Direto)
    public Usuario banirUsuario(Long adminId, Long id) {
        // Verifica se quem pediu é admin
        Usuario admin = repository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));
        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso Negado. Apenas administradores podem banir contas.");
        }

        Usuario usuario = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        usuario.banirConta();
        return repository.save(usuario);
    }

    // Caso de Uso: Ver o Ranking (Exclusivo para Administradores)
    public List<Usuario> listarRankingAdmin(Long adminId) {
        
        // 1. Verifica quem está pedindo o ranking
        Usuario admin = repository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));

        // 2. Se não for ADMIN, barra na porta!
        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso Negado. Apenas administradores podem ver o ranking completo.");
        }

        // 3. Se passou na verificação, devolve a lista completa e ordenada
        return repository.findAllByOrderByXpTotalDesc();
    }

    // Caso de Uso: Ver o Ranking (Apenas para jogadores cadastrados)
    public List<Usuario> listarRankingParaUsuario(Long usuarioId) {
        
        // 1. Verifica se quem está pedindo o ranking realmente tem uma conta
        repository.findById(usuarioId)
                .orElseThrow(() -> new SecurityException("Acesso Negado. Você precisa ter uma conta ativa para ver o ranking."));

        // 2. Se a conta existe, devolve a lista para o Controller montar a bandeja
        return repository.findAllByOrderByXpTotalDesc();
    }

    // 1. Listagem Geral (Para jogadores comuns)
    public List<Usuario> listarTodosParaUsuario(Long usuarioId) {
        // Exige que quem está pedindo a lista tenha uma conta válida
        repository.findById(usuarioId)
                .orElseThrow(() -> new SecurityException("Acesso Negado. Você precisa de uma conta ativa."));
        
        return repository.findAll();
    }

    // Caso de Uso: Listar todos os usuários (Exclusivo para Administradores)
    public List<Usuario> listarTodosParaAdmin(Long adminId) {

        Usuario admin = repository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador não encontrado."));

        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso Negado. Apenas administradores podem ver a lista completa.");
        }

        return repository.findAll();
    }

    // Caso de Uso: Buscar um usuário específico pelo ID (Exclusivo para Administradores)
    public Usuario buscarPorIdParaAdmin(Long adminId, Long usuarioBuscadoId) {
        
        // 1. Verifica quem está pedindo a informação
        Usuario admin = repository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));

        // 2. Se não for ADMIN, barra na porta!
        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso Negado. Apenas administradores podem ver o perfil completo de outro usuário.");
        }

        // 3. Se for Admin, busca o usuário alvo no banco e devolve a panela inteira
        return repository.findById(usuarioBuscadoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário buscado não encontrado no sistema."));
    }
    
    // Caso de Uso: Administrador a eliminar um utilizador (membro) do sistema
    public void deletarUsuarioPorAdmin(Long adminId, Long membroId) {
        
        // 1. Verifica quem está a tentar fazer a ação
        Usuario admin = repository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador solicitante não encontrado."));

        // 2. Se não for ADMIN, bloqueia a ação imediatamente!
        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso Negado. Apenas administradores podem eliminar membros do sistema.");
        }

        // 3. Busca o membro que vai ser eliminado
        Usuario membro = repository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("O membro que tentou eliminar não foi encontrado."));

        // 4. Se chegou até aqui com sucesso, elimina o membro!
        repository.delete(membro);
    }

    // Caso de Uso: Membro deletar a própria conta
    public void deletarPropriaConta(Long usuarioId) {
        
        // 1. Busca o membro pelo ID que veio no crachá
        Usuario membro = repository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado no sistema."));

        // 2. Apaga a conta dele permanentemente
        repository.delete(membro);
    }

    // Caso de Uso: Atualizar dados do próprio usuário
    public Usuario atualizarUsuario(Long usuarioId, Usuario dadosAtualizados) {
        // Busca o usuário pelo ID do crachá
        Usuario usuarioExistente = repository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // Atualiza apenas os campos permitidos (nome, email, senha)
        usuarioExistente.setNome(dadosAtualizados.getNome());
        usuarioExistente.setEmail(dadosAtualizados.getEmail());
        
        // Se ele mandou uma senha nova, a gente atualiza (lembrando de criptografar na vida real!)
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isEmpty()) {
            usuarioExistente.setSenha(dadosAtualizados.getSenha());
        }

        return repository.save(usuarioExistente);
    }

    public Usuario atualizarUsuarioPorAdmin(Long adminId, Long usuarioAlvoId, Usuario dadosAtualizados) {
        
        Usuario admin = repository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin solicitante não encontrado."));

        if (admin.getPerfil() != Usuario.Perfil.ADMIN) {
            throw new SecurityException("Acesso Negado. Apenas Admins podem editar outros usuários.");
        }

        Usuario alvo = repository.findById(usuarioAlvoId)
                .orElseThrow(() -> new IllegalArgumentException("Membro alvo não encontrado."));

        if (dadosAtualizados.getNome() != null) {
            alvo.setNome(dadosAtualizados.getNome());
        }
        
        if (dadosAtualizados.getEmail() != null) {
            alvo.setEmail(dadosAtualizados.getEmail());
        }
        
        // 🔒 A MÁGICA ACONTECE AQUI:
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isEmpty()) {
            // Pega a senha nova, passa no triturador (encode) e salva o hash!
            String senhaCriptografada = passwordEncoder.encode(dadosAtualizados.getSenha());
            alvo.setSenha(senhaCriptografada);
        }

        // Se o admin enviou um perfil novo, atualiza!
        if (dadosAtualizados.getPerfil() != null) {
            alvo.setPerfil(dadosAtualizados.getPerfil());
        }

        return repository.save(alvo);
    }

    // Caso de Uso: Membro atualiza o PRÓPRIO perfil
    public Usuario atualizarProprioPerfil(Long usuarioId, Usuario dadosAtualizados) {
        
        // 1. Busca o usuário pelo ID do crachá
        Usuario usuarioExistente = repository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado no sistema."));

        // 2. Atualiza os dados básicos (só se ele mandou algo novo)
        if (dadosAtualizados.getNome() != null) {
            usuarioExistente.setNome(dadosAtualizados.getNome());
        }
        
        if (dadosAtualizados.getEmail() != null) {
            usuarioExistente.setEmail(dadosAtualizados.getEmail());
        }
        
        // 3. A MÁGICA DA CRIPTOGRAFIA AQUI TAMBÉM! 🔒
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isEmpty()) {
            String senhaCriptografada = passwordEncoder.encode(dadosAtualizados.getSenha());
            usuarioExistente.setSenha(senhaCriptografada);
        }

        // * O perfil, XP e conquistas NUNCA são alterados por aqui!

        // 4. Salva e retorna a entidade atualizada
        return repository.save(usuarioExistente);
    }

    // Caso de Uso: Jogador comum busca os dados do próprio perfil para o Dashboard
    public Usuario buscarMeuPerfil(Long usuarioId) {
        return repository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado no sistema."));
    }

    
}