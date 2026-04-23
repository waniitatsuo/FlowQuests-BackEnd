package ex.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "usuario")
public class Usuario {

    public enum Perfil { USER, ADMIN }
    public enum StatusConta { ATIVO, BLOQUEADO, BANIDO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Column(name = "xp_total")
    private int xpTotal = 0; // Já começa com 0 para evitar nulos
    
    @Enumerated(EnumType.STRING) // <-- Aqui está a ligação com o banco!
    @Column(name = "perfil", nullable = false)
    private Perfil perfil = Perfil.USER; 

    @Enumerated(EnumType.STRING)
    @Column(name = "status_conta", nullable = false)
    private StatusConta statusConta = StatusConta.ATIVO; 
    
    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime criadoEm;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Tarefa> tarefas;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
      name = "usuario_conquistas", 
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "conquista_id")
    )
    private Set<Conquista> conquistas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Advertencia> advertencias;
    
    // --- Construtor Padrão (Obrigatório para o Hibernate) ---
    public Usuario() {}

    // --- Getters (Podemos ler tudo sem problemas) ---
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public int getXpTotal() { return xpTotal; }
    public Perfil getPerfil() { return perfil; }
    public StatusConta getStatusConta() { return statusConta; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public List<Tarefa> getTarefas() { return tarefas; }
    public Set<Conquista> getConquistas() { return conquistas; }
    public List<Advertencia> getAdvertencias() { return advertencias; }

    // --- Setters Seguros (Apenas para o que realmente pode ser alterado diretamente) ---
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setSenha(String senha) { this.senha = senha; }

    // --- MÉTODOS DE NEGÓCIO (Adeus setters anêmicos, olá encapsulação!) ---
    
    public void adicionarXp(int xp) {
        if (xp > 0) { // Garante que ninguém vai passar XP negativo por engano
            this.xpTotal += xp;
        }
    }

    public void promoverParaAdmin() {
        this.perfil = Perfil.ADMIN;
    }

    public void bloquearConta() {
        this.statusConta = StatusConta.BLOQUEADO;
    }

    public void banirConta() {
        this.statusConta = StatusConta.BANIDO;
    }
    
    public void reativarConta() {
        this.statusConta = StatusConta.ATIVO;
    }
}