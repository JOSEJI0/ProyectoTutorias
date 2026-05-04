package itch.tspw.ProyectoTutorias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itch.tspw.ProyectoTutorias.model.ActividadPat;
import itch.tspw.ProyectoTutorias.model.ActividadPatGrupo;
import itch.tspw.ProyectoTutorias.model.GrupoTutoria;
import itch.tspw.ProyectoTutorias.model.PatGrupo;
import itch.tspw.ProyectoTutorias.model.PatInstitucional;
import itch.tspw.ProyectoTutorias.repository.ActividadPatGrupoRepository;
import itch.tspw.ProyectoTutorias.repository.GrupoTutoriaRepository;
import itch.tspw.ProyectoTutorias.repository.PatGrupoRepository;
import itch.tspw.ProyectoTutorias.repository.PatRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PatGrupoService {

    @Autowired
    private PatGrupoRepository patGrupoRepository;

    @Autowired
    private ActividadPatGrupoRepository actividadPatGrupoRepository;

    @Autowired
    private PatService patService;

    @Autowired
    private ActividadPatService actividadPatService;

    @Autowired
    private GrupoTutoriaService grupoService;
    
    @Autowired
    private PatRepository patRepository;
    
    @Autowired
    private GrupoTutoriaRepository grupoTutoriaRepository;
    // ---------------------------------------------

    @Transactional
    public PatGrupo clonarPatParaGrupo(Integer idGrupo, Integer idPatInstitucional) {
        Optional<PatGrupo> patExistente = patGrupoRepository.findByGrupo_IdGrupo(idGrupo);
        if (patExistente.isPresent()) {
            throw new RuntimeException("Este grupo ya cuenta con un PAT adaptado.");
        }

        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        PatInstitucional patInst = patService.obtenerPorId(idPatInstitucional);

        PatGrupo nuevoPatGrupo = new PatGrupo();
        nuevoPatGrupo.setGrupo(grupo);
        nuevoPatGrupo.setPatInstitucionalOrigen(patInst);
        nuevoPatGrupo.setFechaAdaptacion(LocalDate.now());
        
        PatGrupo patGuardado = patGrupoRepository.save(nuevoPatGrupo);

        List<ActividadPat> actividadesInstitucionales = actividadPatService.listarPorPat(idPatInstitucional);

        for (ActividadPat actInst : actividadesInstitucionales) {
            ActividadPatGrupo actGrupo = new ActividadPatGrupo();
            actGrupo.setPatGrupo(patGuardado);
            actGrupo.setTitulo(actInst.getTitulo());
            actGrupo.setDescripcion(actInst.getDescripcion());
            actGrupo.setSemanaProgramada(actInst.getSemanaProgramada());
            actGrupo.setEstatus("Pendiente"); 
            
            actividadPatGrupoRepository.save(actGrupo);
        }

        return patGuardado;
    }

    public PatGrupo obtenerPatDeGrupo(Integer idGrupo) {
        return patGrupoRepository.findByGrupo_IdGrupo(idGrupo).orElse(null);
    }
    
    public List<ActividadPatGrupo> obtenerActividadesDeGrupo(Integer idPatGrupo) {
        return actividadPatGrupoRepository.findByPatGrupo_IdPatGrupoOrderBySemanaProgramadaAsc(idPatGrupo);
    }

    @Transactional
    public void actualizarActividad(Integer idActividadGrupo, String titulo, String descripcion, String estatus) {
        ActividadPatGrupo act = actividadPatGrupoRepository.findById(idActividadGrupo)
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        
        act.setTitulo(titulo);
        act.setDescripcion(descripcion);
        act.setEstatus(estatus);
        
        actividadPatGrupoRepository.save(act);
    }

    @Transactional
    public void asignarPatAutomatico(GrupoTutoria grupo) {
        Optional<PatInstitucional> patInst = patRepository.findByPeriodo_IdPeriodoAndCarrera_IdCarrera(
                grupo.getPeriodo().getIdPeriodo(), 
                grupo.getCarrera().getIdCarrera()
        );

        if (patInst.isPresent()) {
            if (patGrupoRepository.findByGrupo_IdGrupo(grupo.getIdGrupo()).isEmpty()) {
                clonarPatParaGrupo(grupo.getIdGrupo(), patInst.get().getIdPat());
            }
        }
    }

    @Transactional
    public void asignarPatAGruposExistentes(PatInstitucional patInst) {
        List<GrupoTutoria> grupos = grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(true);
        
        for (GrupoTutoria grupo : grupos) {
            if (grupo.getCarrera().getIdCarrera().equals(patInst.getCarrera().getIdCarrera())) {
                if (patGrupoRepository.findByGrupo_IdGrupo(grupo.getIdGrupo()).isEmpty()) {
                    clonarPatParaGrupo(grupo.getIdGrupo(), patInst.getIdPat());
                }
            }
        }
    }
}