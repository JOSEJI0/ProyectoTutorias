package itch.tspw.ProyectoTutorias.model;

import jakarta.persistence.*;

@Entity
@Table(name = "carreras")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(name = "nombre_carrera", length = 150, nullable = false)
    private String nombreCarrera;

    @Column(name = "clave_oficial", length = 20, unique = true)
    private String claveOficial;

	public Integer getIdCarrera() {
		return idCarrera;
	}

	public void setIdCarrera(Integer idCarrera) {
		this.idCarrera = idCarrera;
	}

	public String getNombreCarrera() {
		return nombreCarrera;
	}

	public void setNombreCarrera(String nombreCarrera) {
		this.nombreCarrera = nombreCarrera;
	}

	public String getClaveOficial() {
		return claveOficial;
	}

	public void setClaveOficial(String claveOficial) {
		this.claveOficial = claveOficial;
	}
    
    
}