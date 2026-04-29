package itch.tspw.ProyectoTutorias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tutores")
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tutor")
    private Integer idTutor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "rfc_empleado", length = 20, unique = true, nullable = false)
    private String rfcEmpleado;

	public Integer getIdTutor() {
		return idTutor;
	}

	public void setIdTutor(Integer idTutor) {
		this.idTutor = idTutor;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getRfcEmpleado() {
		return rfcEmpleado;
	}

	public void setRfcEmpleado(String rfcEmpleado) {
		this.rfcEmpleado = rfcEmpleado;
	}
    
}