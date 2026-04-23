package ex.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas")
public class Tarefa {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tarefa_id")
    private Long id;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "data_prazo")
    private LocalDate dataPrazo;

    @Column(name = "hora_prazo")
    private LocalTime horaPrazo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private Estado estado = Estado.pendente;

    @Column(name = "recompensa_xp")
    private int recompensaXp;
    
    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference 
    private Usuario usuario;
    
    public enum Categoria {
        remedios, atividades, trabalhos, eventos
    }

    public enum Estado {
        pendente, concluida, atrasada
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDate dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public LocalTime getHoraPrazo() {
        return horaPrazo;
    }

    public void setHoraPrazo(LocalTime horaPrazo) {
        this.horaPrazo = horaPrazo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public int getRecompensaXp() {
        return recompensaXp;
    }

    public void setRecompensaXp(int recompensaXp) {
        this.recompensaXp = recompensaXp;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}