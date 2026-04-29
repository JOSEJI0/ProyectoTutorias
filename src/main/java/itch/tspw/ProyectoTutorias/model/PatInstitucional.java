package itch.tspw.ProyectoTutorias.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "pat_institucional")
public class PatInstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pat")
    private Integer idPat;

    @ManyToOne
    @JoinColumn(name = "id_periodo", nullable = false)
    private PeriodoEscolar periodo;

    @Column(length = 20)
    private String version;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    public PatInstitucional() {}

    public Integer getIdPat() { return idPat; }
    public void setIdPat(Integer idPat) { this.idPat = idPat; }
    public PeriodoEscolar getPeriodo() { return periodo; }
    public void setPeriodo(PeriodoEscolar periodo) { this.periodo = periodo; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
}