package ex.dto;

import ex.model.Usuario;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UsuarioPublicoDTO {
    
    private Long id;
    private String nome;
    private String email;
    private int xpTotal;
    private String perfil;

    // Construtor que converte a Entidade (Usuario) para o DTO automaticamente
    public UsuarioPublicoDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.xpTotal = usuario.getXpTotal();
        this.perfil = usuario.getPerfil().name();
    }
}