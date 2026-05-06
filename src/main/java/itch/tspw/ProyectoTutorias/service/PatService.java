package itch.tspw.ProyectoTutorias.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tspw.ProyectoTutorias.model.PatInstitucional;
import itch.tspw.ProyectoTutorias.repository.PatRepository;

import java.util.List;

@Service
public class PatService {
    @Autowired
    private PatRepository patRepository;


    public PatInstitucional guardar(PatInstitucional pat) {
        return patRepository.save(pat);
    }

    public void eliminar(Integer idPat) {
        patRepository.deleteById(idPat);
    }
    
    public List<PatInstitucional> listarTodos() {
        return patRepository.findByActivoTrueOrderByIdPatDesc();
    }
    
    public PatInstitucional obtenerPorId(Integer idPat) {
        return patRepository.findById(idPat).orElseThrow(() -> new RuntimeException("PAT no encontrado"));
    }

    // 2. Actualiza la verificación de duplicados
    public boolean existePatParaCarreraEnPeriodo(Integer idPeriodo, Integer idCarrera) {
        return patRepository.findByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrue(idPeriodo, idCarrera).isPresent();
    }

    // 3. CAMBIA el borrado físico por LÓGICO
    public void eliminarLogico(Integer idPat) {
        PatInstitucional pat = patRepository.findById(idPat)
            .orElseThrow(() -> new RuntimeException("PAT no encontrado"));
        pat.setActivo(false);
        patRepository.save(pat);
    }
}