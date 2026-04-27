package ex.controller;

import ex.model.Usuario;
import ex.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import ex.dto.UsuarioPublicoDTO;
import ex.dto.UsuarioRankingDTO;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {
	
    // Agora injetamos o SERVICE, e não mais o Repository ou o PasswordEncoder!
    @Autowired
    private UsuarioService usuarioService;

    // Endpoint: Criar conta (Público)
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario novoUsuario) {
        try {
            // O Service vai cuidar de criptografar a senha e setar o perfil como USER
            Usuario usuarioSalvo = usuarioService.registrarUsuario(novoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioPublicoDTO(usuarioSalvo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para Criar um Novo Administrador do zero
    @PostMapping("/admin/adicionarADM")
    public ResponseEntity<?> criarAdmin(
            @RequestHeader("X-Admin-Id") Long adminId, 
            @RequestBody Usuario novoAdmin) {
        try {
            // Usa o seu service para registrar o cara já com o Perfil de ADMIN
            Usuario adminSalvo = usuarioService.registrarAdministrador(adminId, novoAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(adminSalvo);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Caso de Uso: Logar no sistema
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario dadosLogin) {
        Usuario usuarioLogado = usuarioService.realizarLogin(dadosLogin.getEmail(), dadosLogin.getSenha());

        if (usuarioLogado != null) {
            return ResponseEntity.ok(usuarioLogado); // 200 - OK
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 - Falha no login ou conta inativa
        }
    }

    // Endpoints de Moderação (Protegidos)
    @PutMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquear(
            @RequestHeader("X-Admin-Id") Long adminId,
            @PathVariable Long id) {
        try {
            Usuario usuarioBloqueado = usuarioService.bloquearUsuario(adminId, id);
            return ResponseEntity.ok(usuarioBloqueado);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/banir")
    public ResponseEntity<?> banir(
            @RequestHeader("X-Admin-Id") Long adminId,
            @PathVariable Long id) {
        try {
            Usuario usuarioBanido = usuarioService.banirUsuario(adminId, id);
            return ResponseEntity.ok(usuarioBanido);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Buscas padrão
    // Endpoint para Listar Todos (Protegido - Só Usuários Cadastrados recebem o DTO limpo)
    @GetMapping("/geral")
    public ResponseEntity<?> listarTodosGeral(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            List<Usuario> listaCompleta = usuarioService.listarTodosParaUsuario(usuarioId);
            
            // Converte a lista gorda na lista blindada de DTOs!
            List<UsuarioPublicoDTO> listaFiltrada = listaCompleta.stream()
                    .map(usuario -> new UsuarioPublicoDTO(usuario))
                    .toList();
                    
            return ResponseEntity.ok(listaFiltrada);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Endpoint: Retorna a lista completa de usuários para o painel Admin
    @GetMapping("/admin")
    public ResponseEntity<?> listarTodosAdmin(@RequestHeader("X-Admin-Id") Long adminId) {
        try {
            // Pega a lista completa da cozinha
            List<Usuario> listaCompleta = usuarioService.listarTodosParaAdmin(adminId);

            // Devolve pro Node.js
            return ResponseEntity.ok(listaCompleta);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para Buscar Usuário por ID (Protegido - Só Admins recebem a entidade inteira)
    @GetMapping("/admin/{id}")
    public ResponseEntity<?> buscarPorIdAdmin(
            @RequestHeader("X-Admin-Id") Long adminId,
            @PathVariable Long id) {
        try {
            // Chama o Service passando o crachá do Admin e o ID do alvo
            Usuario usuarioCompleto = usuarioService.buscarPorIdParaAdmin(adminId, id);
            
            // Retorna o usuário com todos os dados sensíveis liberados
            return ResponseEntity.ok(usuarioCompleto);
            
        } catch (SecurityException e) {
            // 403 Forbidden - O espertinho não é admin
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // 400 Bad Request - O admin não existe ou o usuário buscado não existe
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para o Administrador eliminar um Membro (Protegido)
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deletarMembro(
            @RequestHeader("X-Admin-Id") Long adminId,
            @PathVariable Long id) {
        try {
            // Chama o Service passando o crachá do admin e o ID do membro a eliminar
            usuarioService.deletarUsuarioPorAdmin(adminId, id);
            
            // Retorna um 200 OK com mensagem de sucesso
            return ResponseEntity.ok("Membro eliminado permanentemente do sistema com sucesso.");
            
        } catch (SecurityException e) {
            // 403 Forbidden - O utilizador comum tentou apagar alguém
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // 400 Bad Request - O admin ou o membro não existem
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para o Membro deletar a PRÓPRIA conta
    @DeleteMapping("/geral/deletar-conta")
    public ResponseEntity<?> deletarMinhaConta(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            // Chama o Service passando o crachá do membro
            usuarioService.deletarPropriaConta(usuarioId);
            
            // Retorna um 200 OK de despedida
            return ResponseEntity.ok("Sua conta foi excluída com sucesso. Sentiremos sua falta! 👋");
            
        } catch (IllegalArgumentException e) {
            // 400 Bad Request - O ID passado no header não existe
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para Ver o Ranking (Protegido - Só Usuários Cadastrados recebem o DTO)
    @GetMapping("/geral/ranking")
    public ResponseEntity<?> verRanking(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            // 1. A cozinha verifica se o usuárioId existe e devolve a lista gorda
            List<Usuario> rankingCompleto = usuarioService.listarRankingParaUsuario(usuarioId);
            
            // 2. O garçom converte a lista gorda na nossa bandeja segura (DTO Imutável)
            List<UsuarioRankingDTO> rankingFiltrado = rankingCompleto.stream()
                    .map(usuario -> new UsuarioRankingDTO(usuario))
                    .toList(); 
                    
            // 3. Entrega o DTO limpinho!
            return ResponseEntity.ok(rankingFiltrado);
            
        } catch (SecurityException e) {
            // 403 Forbidden - Visitante anônimo ou ID falso
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Endpoint para Ver o Ranking Completo (Protegido - Só Admins recebem a entidade inteira)
    @GetMapping("/admin/ranking")
    public ResponseEntity<?> verRankingAdmin(@RequestHeader("X-Admin-Id") Long adminId) {
        try {
            // 1. Chama o Service passando o crachá do Admin
            // Se ele não for admin, o Service vai jogar a SecurityException e cair no catch lá embaixo!
            List<Usuario> rankingCompleto = usuarioService.listarRankingAdmin(adminId);
            
            // 2. Retorna a lista completa com todos os dados sensíveis liberados para moderação
            return ResponseEntity.ok(rankingCompleto);
            
        } catch (SecurityException e) {
            // 403 Forbidden - Usuário comum tentou dar uma de espertinho na rota de admin
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // 400 Bad Request - O ID passado no header não existe no banco
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para o Administrador editar um Usuário (Protegido)
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> editarPorAdmin(
            @RequestHeader("X-Admin-Id") Long adminId,
            @PathVariable Long id,
            @RequestBody Usuario dadosAtualizados) {
        try {
            // Manda a requisição para a cozinha
            Usuario usuarioAtualizado = usuarioService.atualizarUsuarioPorAdmin(adminId, id, dadosAtualizados);
            
            // Retorna 200 OK com os dados novos salvos
            return ResponseEntity.ok(usuarioAtualizado);
            
        } catch (SecurityException e) {
            // 403 Forbidden - Usuário comum tentou dar uma de espertinho
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // 400 Bad Request - ID do admin ou do alvo não existe
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint: Membro altera os PRÓPRIOS dados
    @PutMapping("/geral/atualizar")
    public ResponseEntity<?> atualizarMinhaConta(
            @RequestHeader("X-Usuario-Id") Long usuarioId, // Pega o ID direto do crachá!
            @RequestBody Usuario dadosAtualizados) {
        try {
            // Manda a cozinha trabalhar
            Usuario usuarioSalvo = usuarioService.atualizarProprioPerfil(usuarioId, dadosAtualizados);
            
            // Devolve a bandeja blindada (DTO)
            return ResponseEntity.ok(new UsuarioPublicoDTO(usuarioSalvo));
            
        } catch (IllegalArgumentException e) {
            // 400 Bad Request - Usuário não existe
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint: Jogador carrega o próprio perfil
    @GetMapping("/geral/meu-perfil")
    public ResponseEntity<?> carregarMeuPerfil(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            Usuario meuPerfil = usuarioService.buscarMeuPerfil(usuarioId);

            // DICA DE OURO: Para a senha não ir para o Node.js de bobeira,
            // a gente limpa ela do objeto só nessa resposta (não apaga do banco)
            meuPerfil.setSenha(null);

            return ResponseEntity.ok(meuPerfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}