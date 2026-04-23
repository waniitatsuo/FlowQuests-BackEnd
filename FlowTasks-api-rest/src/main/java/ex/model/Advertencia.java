package ex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "advertencias")
public class Advertencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advertencia_id")
    private Long id;

    @Column(name = "motivo", nullable = false, length = 100)
    private String motivo;

    @Column(name = "detalhamento", columnDefinition = "TEXT")
    private String detalhamento;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    // --- Construtor Padrão ---
    public Advertencia() {}

    // --- Getters ---
    public Long getId() { return id; }
    public String getMotivo() { return motivo; }
    public String getDetalhamento() { return detalhamento; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public Usuario getUsuario() { return usuario; }

    // --- Setters Seguros ---
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setDetalhamento(String detalhamento) { this.detalhamento = detalhamento; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}