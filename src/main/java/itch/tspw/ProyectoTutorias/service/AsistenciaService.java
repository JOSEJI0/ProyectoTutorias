package itch.tspw.ProyectoTutorias.service;

import org.springframework.stereotype.Service;

import itch.tspw.ProyectoTutorias.repository.AsistenciaRepository;

@Service
public class AsistenciaService {

	private final AsistenciaRepository asistenciaRepository;
    private static final int SESIONES_SEMESTRE = 10;

    public AsistenciaService(AsistenciaRepository asistenciaRepository) {
        this.asistenciaRepository = asistenciaRepository;
    }
    public double calcularPorcentajeAsistencia(Integer idEstudiante) {
        long asistencias = asistenciaRepository.countByEstudiante_IdEstudianteAndPresenteTrue(idEstudiante);
        return ((double) asistencias / SESIONES_SEMESTRE) * 100.0;
    }

    public boolean esCandidatoALiberacion(Integer idEstudiante) {
        return calcularPorcentajeAsistencia(idEstudiante) >= 80.0;
    }

    public boolean tieneRiesgoDesercion(Integer idEstudiante) {
        long faltas = asistenciaRepository.countByEstudiante_IdEstudianteAndPresenteFalse(idEstudiante);
        return faltas >= 2;
    }
}