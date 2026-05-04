package itch.tspw.ProyectoTutorias.model;

import jakarta.persistence.*;

@Entity
@Table(name = "actividades_pat")
public class ActividadPat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idActividad;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Integer semanaProgramada;

    @ManyToOne
    @JoinColumn(name = "id_pat", nullable = false)
    private PatInstitucional pat;
    
    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activo = true;

	public Integer getIdActividad() {
		return idActividad;
	}

	public void setIdActividad(Integer idActividad) {
		this.idActividad = idActividad;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getSemanaProgramada() {
		return semanaProgramada;
	}

	public void setSemanaProgramada(Integer semanaProgramada) {
		this.semanaProgramada = semanaProgramada;
	}

	public PatInstitucional getPat() {
		return pat;
	}

	public void setPat(PatInstitucional pat) {
		this.pat = pat;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
	
   
}