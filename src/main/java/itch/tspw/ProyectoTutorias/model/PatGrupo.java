package itch.tspw.ProyectoTutorias.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pat_grupos")
public class PatGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pat_grupo")
    private Integer idPatGrupo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", nullable = false)
    private GrupoTutoria grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pat_institucional")
    private PatInstitucional patInstitucionalOrigen;

    @Column(name = "fecha_adaptacion", nullable = false)
    private LocalDate fechaAdaptacion;

    // Getters y Setters
    public Integer getIdPatGrupo() { return idPatGrupo; }
    public void setIdPatGrupo(Integer idPatGrupo) { this.idPatGrupo = idPatGrupo; }

    public GrupoTutoria getGrupo() { return grupo; }
    public void setGrupo(GrupoTutoria grupo) { this.grupo = grupo; }

    public PatInstitucional getPatInstitucionalOrigen() { return patInstitucionalOrigen; }
    public void setPatInstitucionalOrigen(PatInstitucional patInstitucionalOrigen) { this.patInstitucionalOrigen = patInstitucionalOrigen; }

    public LocalDate getFechaAdaptacion() { return fechaAdaptacion; }
    public void setFechaAdaptacion(LocalDate fechaAdaptacion) { this.fechaAdaptacion = fechaAdaptacion; }
}