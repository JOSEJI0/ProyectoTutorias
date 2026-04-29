package itch.tspw.ProyectoTutorias.model;

import jakarta.persistence.*;

@Entity
@Table(name = "perfiles")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Integer idPerfil;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre; 

    public Perfil() {
    }

    public Perfil(String nombre) {
        this.nombre = nombre;
    }

    // Getters y setters
    public Integer getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Integer idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}