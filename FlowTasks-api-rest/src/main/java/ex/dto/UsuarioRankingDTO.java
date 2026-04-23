package ex.dto;

import ex.model.Usuario;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UsuarioRankingDTO {
    
    private String nome;
    private int xpTotal;

    // Construtor para transformar o usuário da busca em um competidor do ranking
    public UsuarioRankingDTO(Usuario usuario) {
        this.nome = usuario.getNome();
        this.xpTotal = usuario.getXpTotal();
    }
}