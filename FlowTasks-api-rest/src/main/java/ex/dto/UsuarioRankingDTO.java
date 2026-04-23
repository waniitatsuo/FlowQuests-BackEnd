package ex.dto;

import ex.model.Usuario;

// O "record" já cria tudo imutável automaticamente!
public record UsuarioRankingDTO(String nome, int xpTotal) {
    
    // Construtor que facilita a conversão
    public UsuarioRankingDTO(Usuario usuario) {
        this(usuario.getNome(), usuario.getXpTotal());
    }
}