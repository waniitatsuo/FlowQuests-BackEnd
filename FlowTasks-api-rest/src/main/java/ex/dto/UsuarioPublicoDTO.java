package ex.dto;

import ex.model.Usuario;

// Usando o poder supremo do "record" para ser imutável!
public record UsuarioPublicoDTO(Long id, String nome, int xpTotal, String perfil) {
    
    // Construtor mágico que converte a entidade gorda na bandeja limpinha
    public UsuarioPublicoDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getXpTotal(), usuario.getPerfil().name());
    }
}