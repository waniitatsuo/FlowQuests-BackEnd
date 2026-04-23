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

    public enum Categoria { remedios, atividades, trabalhos, eventos }
    public enum Estado { pendente, concluida, atrasada }

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

    // --- Construtor ---
    public Tarefa() {}

    // --- Getters ---
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public LocalDate getDataPrazo() { return dataPrazo; }
    public LocalTime getHoraPrazo() { return horaPrazo; }
    public Categoria getCategoria() { return categoria; }
    public Estado getEstado() { return estado; }
    public int getRecompensaXp() { return recompensaXp; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public Usuario getUsuario() { return usuario; }

    // --- Setters Seguros ---
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDataPrazo(LocalDate dataPrazo) { this.dataPrazo = dataPrazo; }
    public void setHoraPrazo(LocalTime horaPrazo) { this.horaPrazo = horaPrazo; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    // --- Métodos de Negócio ---
    
    // Calcula o XP baseado na categoria informada
    public void calcularRecompensaXp() {
        if (this.categoria != null) {
            switch (this.categoria) {
                case remedios: this.recompensaXp = 70; break;
                case atividades: this.recompensaXp = 40; break;
                case trabalhos: this.recompensaXp = 50; break;
                case eventos: this.recompensaXp = 30; break;
                default: this.recompensaXp = 0;
            }
        }
    }

    public void marcarComoConcluida() {
        this.estado = Estado.concluida;
    }
}