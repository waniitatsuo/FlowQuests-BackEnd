package ex.controller;

import ex.model.Usuario;
import ex.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {
	
    // Agora injetamos o SERVICE, e não mais o Repository ou o PasswordEncoder!
    @Autowired
    private UsuarioService usuarioService;

    // Caso de Uso: Se Registrar
    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody Usuario novoUsuario) {
        Usuario usuarioSalvo = usuarioService.registrarUsuario(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
    }

    // Caso de Uso: Criar conta de administrador
    @PostMapping("/admin")
    public ResponseEntity<Usuario> criarAdmin(@RequestBody Usuario novoAdmin) {
        Usuario adminSalvo = usuarioService.registrarAdministrador(novoAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminSalvo);
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

    // Endpoints de Moderação
    @PutMapping("/{id}/bloquear")
    public ResponseEntity<Usuario> bloquear(@PathVariable Long id) {
        try {
            Usuario usuarioBloqueado = usuarioService.bloquearUsuario(id);
            return ResponseEntity.ok(usuarioBloqueado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/banir")
    public ResponseEntity<Usuario> banir(@PathVariable Long id) {
        try {
            Usuario usuarioBanido = usuarioService.banirUsuario(id);
            return ResponseEntity.ok(usuarioBanido);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Buscas padrão
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.buscarPorId(id);
        return usuario.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (usuarioService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}