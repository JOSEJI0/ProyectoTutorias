package itch.tspw.ProyectoTutorias.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "necesidades_estudiantes")
public class NecesidadEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_necesidad")
    private Integer idNecesidad;

    @ManyToOne
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @Column(name = "area", nullable = false)
    private String area;

    @Column(name = "descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud = LocalDate.now();

    @Column(name = "estatus")
    private String estatus = "Pendiente"; // Pendiente, En Seguimiento, Atendida

    // Getters y Setters
    public Integer getIdNecesidad() { return idNecesidad; }
    public void setIdNecesidad(Integer idNecesidad) { this.idNecesidad = idNecesidad; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDate fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}