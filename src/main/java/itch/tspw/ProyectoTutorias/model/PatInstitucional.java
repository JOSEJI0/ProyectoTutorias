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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo", nullable = false)
    private PeriodoEscolar periodo;

    // NUEVO: Relación con Carrera
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @Column(length = 20)
    private String version;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;
    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activo = true;
    
    public PatInstitucional() {}

    // Getters y setters
    public Integer getIdPat() { return idPat; }
    public void setIdPat(Integer idPat) { this.idPat = idPat; }
    
    public PeriodoEscolar getPeriodo() { return periodo; }
    public void setPeriodo(PeriodoEscolar periodo) { this.periodo = periodo; }
    
    public Carrera getCarrera() { return carrera; }
    public void setCarrera(Carrera carrera) { this.carrera = carrera; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
    
}