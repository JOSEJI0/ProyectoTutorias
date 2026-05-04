package itch.tspw.ProyectoTutorias.model;

import jakarta.persistence.*;

@Entity
@Table(name = "actividades_pat_grupo")
public class ActividadPatGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad_grupo")
    private Integer idActividadGrupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pat_grupo", nullable = false)
    private PatGrupo patGrupo;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "semana_programada")
    private Integer semanaProgramada;

    // Nos servirá para que el tutor marque la actividad como "Completada" o "Pendiente"
    @Column(length = 50)
    private String estatus = "Pendiente"; 

    // Getters y Setters
    public Integer getIdActividadGrupo() { return idActividadGrupo; }
    public void setIdActividadGrupo(Integer idActividadGrupo) { this.idActividadGrupo = idActividadGrupo; }

    public PatGrupo getPatGrupo() { return patGrupo; }
    public void setPatGrupo(PatGrupo patGrupo) { this.patGrupo = patGrupo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getSemanaProgramada() { return semanaProgramada; }
    public void setSemanaProgramada(Integer semanaProgramada) { this.semanaProgramada = semanaProgramada; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}