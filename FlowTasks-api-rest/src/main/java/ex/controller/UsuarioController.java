package ex.controller;

import ex.model.Usuario;
import ex.service.UsuarioService;
import ex.dto.UsuarioPublicoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {
	
    @Autowired
    private UsuarioService usuarioService;

    // Login (Público)
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario dadosLogin) {
        Usuario usuarioLogado = usuarioService.realizarLogin(dadosLogin.getEmail(), dadosLogin.getSenha());
        return usuarioLogado != null ? ResponseEntity.ok(usuarioLogado) : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Carregar dados para o Dashboard (Protegido por X-Usuario-Id)
    @GetMapping("/geral/meu-perfil")
    public ResponseEntity<?> carregarMeuPerfil(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            Usuario meuPerfil = usuarioService.buscarMeuPerfil(usuarioId);
            meuPerfil.setSenha(null); // Segurança: não manda a senha pro front
            return ResponseEntity.ok(meuPerfil);
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // Atualizar própria conta
    @PutMapping("/geral/atualizar")
    public ResponseEntity<?> atualizarMinhaConta(@RequestHeader("X-Usuario-Id") Long id, @RequestBody Usuario dados) {
        try {
            Usuario salvo = usuarioService.atualizarProprioPerfil(id, dados);
            return ResponseEntity.ok(new UsuarioPublicoDTO(salvo));
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // --- ROTAS DE MODERAÇÃO (ADMIN) ---

    @GetMapping("/admin/lista-users")
    public ResponseEntity<?> listarTodosAdmin(@RequestHeader("X-Admin-Id") Long adminId) {
        try { return ResponseEntity.ok(usuarioService.listarTodosParaAdmin(adminId)); }
        catch (Exception e) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); }
    }

    @PutMapping("/admin/atualiza/{id}")
    public ResponseEntity<?> editarPorAdmin(@RequestHeader("X-Admin-Id") Long adminId, @PathVariable Long id, @RequestBody Usuario dados) {
        try { return ResponseEntity.ok(usuarioService.atualizarUsuarioPorAdmin(adminId, id, dados)); }
        catch (Exception e) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); }
    }

    @DeleteMapping("/admin/deleta/{id}")
    public ResponseEntity<?> deletarMembro(@RequestHeader("X-Admin-Id") Long adminId, @PathVariable Long id) {
        try {
            usuarioService.deletarUsuarioPorAdmin(adminId, id);
            return ResponseEntity.ok("Usuário removido.");
        } catch (Exception e) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); }
    }
    
    // Rota de Promoção a Admin
    @PutMapping("/admin/adicionar-adm/{id}")
    public ResponseEntity<?> promoverParaAdmin(@RequestHeader("X-Admin-Id") Long adminId, @PathVariable Long id, @RequestBody Usuario dados) {
        try { return ResponseEntity.ok(usuarioService.atualizarUsuarioPorAdmin(adminId, id, dados)); }
        catch (Exception e) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); }
    }
}