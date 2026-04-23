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
    private int xpTotal;
    
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
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public int getXpTotal() {
        return xpTotal;
    }

    public void setXpTotal(int xpTotal) {
        this.xpTotal = xpTotal;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }

    public void setTarefas(List<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }
    
    public Set<Conquista> getConquistas() {
        return conquistas;
    }

    public void setConquistas(Set<Conquista> conquistas) {
        this.conquistas = conquistas;
    }

}