package ex.model;

import jakarta.persistence.*;

@Entity
@Table(name = "conquistas")
public class Conquista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conquista_id")
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "icone", length = 50)
    private String icone;

    private int xpNecessario;
    
    // Construtor padrão obrigatório para o JPA/Hibernate
    public Conquista() {}

    // --- Apenas Getters para o ID ---
    public Long getId() {
        return id;
    }

    // --- Getters e Setters para as informações editáveis ---
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public int getXpNecessario() { return xpNecessario; }
}