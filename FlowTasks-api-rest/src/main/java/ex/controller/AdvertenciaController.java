package ex.controller;

import ex.model.Advertencia;
import ex.service.AdvertenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advertencias")
@CrossOrigin("*")
public class AdvertenciaController {

    @Autowired
    private AdvertenciaService advertenciaService;

    // Endpoint para advertir um usuário (Protegido)
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> advertir(
            @RequestHeader("X-Admin-Id") Long adminId, // <-- O crachá do Admin entra aqui!
            @PathVariable Long usuarioId, 
            @RequestBody Advertencia advertencia) {
        try {
            // Passamos o adminId para o Service validar
            Advertencia advertenciaSalva = advertenciaService.aplicarAdvertencia(adminId, usuarioId, advertencia);
            return ResponseEntity.status(HttpStatus.CREATED).body(advertenciaSalva);
            
        } catch (SecurityException e) {
            // 403 Forbidden - O cara existe, mas não tem permissão
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // 400 Bad Request - Erro de dados
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}