package itch.tspw.ProyectoTutorias.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sesiones")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Integer idSesion;

    @ManyToOne
    @JoinColumn(name = "id_grupo", nullable = false)
    private GrupoTutoria grupo;

    @ManyToOne
    @JoinColumn(name = "id_actividad", nullable = false)
    private ActividadPat actividad;

    @Column(name = "semana_numero")
    private Integer semanaNumero;

    @Column(name = "fecha_imparticion")
    private LocalDate fechaImparticion;

    @Column(name = "estatus_registro", length = 50)
    private String estatusRegistro;

	public Integer getIdSesion() {
		return idSesion;
	}

	public void setIdSesion(Integer idSesion) {
		this.idSesion = idSesion;
	}

	public GrupoTutoria getGrupo() {
		return grupo;
	}

	public void setGrupo(GrupoTutoria grupo) {
		this.grupo = grupo;
	}

	public ActividadPat getActividad() {
		return actividad;
	}

	public void setActividad(ActividadPat actividad) {
		this.actividad = actividad;
	}

	public Integer getSemanaNumero() {
		return semanaNumero;
	}

	public void setSemanaNumero(Integer semanaNumero) {
		this.semanaNumero = semanaNumero;
	}

	public LocalDate getFechaImparticion() {
		return fechaImparticion;
	}

	public void setFechaImparticion(LocalDate fechaImparticion) {
		this.fechaImparticion = fechaImparticion;
	}

	public String getEstatusRegistro() {
		return estatusRegistro;
	}

	public void setEstatusRegistro(String estatusRegistro) {
		this.estatusRegistro = estatusRegistro;
	} 
    
}