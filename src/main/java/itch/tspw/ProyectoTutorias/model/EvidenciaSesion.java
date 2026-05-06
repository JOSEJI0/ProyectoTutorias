package itch.tspw.ProyectoTutorias.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "evidencias_sesion")
public class EvidenciaSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Integer idEvidencia;

    @ManyToOne
    @JoinColumn(name = "id_sesion", nullable = false)
    private Sesion sesion;

    @Column(name = "url_archivo", columnDefinition = "TEXT", nullable = false)
    private String urlArchivo;

    @Column(name = "estatus_validacion", length = 50)
    private String estatusValidacion;

    @Column(name = "notas_coordinador", columnDefinition = "TEXT")
    private String notasCoordinador;

    @Column(name = "fecha_subida", updatable = false)
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        fechaSubida = LocalDateTime.now();
    }

	public Integer getIdEvidencia() {
		return idEvidencia;
	}

	public void setIdEvidencia(Integer idEvidencia) {
		this.idEvidencia = idEvidencia;
	}

	public Sesion getSesion() {
		return sesion;
	}

	public void setSesion(Sesion sesion) {
		this.sesion = sesion;
	}

	public String getUrlArchivo() {
		return urlArchivo;
	}

	public void setUrlArchivo(String urlArchivo) {
		this.urlArchivo = urlArchivo;
	}

	public String getEstatusValidacion() {
		return estatusValidacion;
	}

	public void setEstatusValidacion(String estatusValidacion) {
		this.estatusValidacion = estatusValidacion;
	}

	public String getNotasCoordinador() {
		return notasCoordinador;
	}

	public void setNotasCoordinador(String notasCoordinador) {
		this.notasCoordinador = notasCoordinador;
	}

	public LocalDateTime getFechaSubida() {
		return fechaSubida;
	}

	public void setFechaSubida(LocalDateTime fechaSubida) {
		this.fechaSubida = fechaSubida;
	}

    
}