package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PatGrupoService {

    private final PatGrupoRepository patGrupoRepository;
    private final ActividadPatGrupoRepository actividadPatGrupoRepository;
    private final PatService patService;
    private final ActividadPatService actividadPatService;
    private final GrupoTutoriaService grupoService;
    private final PatRepository patRepository;
    private final GrupoTutoriaRepository grupoTutoriaRepository;

    public PatGrupoService(PatGrupoRepository patGrupoRepository,
                           ActividadPatGrupoRepository actividadPatGrupoRepository,
                           PatService patService,
                           ActividadPatService actividadPatService,
                           GrupoTutoriaService grupoService,
                           PatRepository patRepository,
                           GrupoTutoriaRepository grupoTutoriaRepository) {
        this.patGrupoRepository = patGrupoRepository;
        this.actividadPatGrupoRepository = actividadPatGrupoRepository;
        this.patService = patService;
        this.actividadPatService = actividadPatService;
        this.grupoService = grupoService;
        this.patRepository = patRepository;
        this.grupoTutoriaRepository = grupoTutoriaRepository;
    }

    @Transactional
    public PatGrupo clonarPatParaGrupo(Integer idGrupo, Integer idPatInstitucional) {
        if (patGrupoRepository.findByGrupo_IdGrupo(idGrupo).isPresent()) {
            throw new RuntimeException("Este grupo ya cuenta con un PAT adaptado.");
        }

        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        PatInstitucional patInst = patService.obtenerPorId(idPatInstitucional);
        PatGrupo nuevoPatGrupo = new PatGrupo();
        nuevoPatGrupo.setGrupo(grupo);
        nuevoPatGrupo.setPatInstitucionalOrigen(patInst);
        nuevoPatGrupo.setFechaAdaptacion(LocalDate.now());        
        PatGrupo patGuardado = patGrupoRepository.save(nuevoPatGrupo);
        List<ActividadPat> actividadesBase = actividadPatService.listarPorPat(idPatInstitucional);
        List<ActividadPatGrupo> actividadesClonadas = new ArrayList<>();

        for (ActividadPat actInst : actividadesBase) {
            ActividadPatGrupo actGrupo = new ActividadPatGrupo();
            actGrupo.setPatGrupo(patGuardado);
            actGrupo.setTitulo(actInst.getTitulo());
            actGrupo.setDescripcion(actInst.getDescripcion());
            actGrupo.setSemanaProgramada(actInst.getSemanaProgramada());
            actGrupo.setEstatus("Pendiente"); 
            actividadesClonadas.add(actGrupo);
        }
        actividadPatGrupoRepository.saveAll(actividadesClonadas);
        return patGuardado;
    }

    @Transactional(readOnly = true)
    public PatGrupo obtenerPatDeGrupo(Integer idGrupo) {
        return patGrupoRepository.findByGrupo_IdGrupo(idGrupo).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<ActividadPatGrupo> obtenerActividadesDeGrupo(Integer idPatGrupo) {
        return actividadPatGrupoRepository.findByPatGrupo_IdPatGrupoOrderBySemanaProgramadaAsc(idPatGrupo);
    }

    @Transactional(readOnly = true)
    public ActividadPatGrupo obtenerActividadPorId(Integer idActividad) {
        return actividadPatGrupoRepository.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
    }

    @Transactional
    public void actualizarActividadCompleta(Integer idActividad, String titulo, String descripcion, String estatus, Integer semana) {
        ActividadPatGrupo act = obtenerActividadPorId(idActividad);
        act.setTitulo(titulo);
        act.setDescripcion(descripcion);
        act.setEstatus(estatus);
        act.setSemanaProgramada(semana);
        actividadPatGrupoRepository.save(act);
    }

    @Transactional
    public void eliminarActividad(Integer idActividad) {
        actividadPatGrupoRepository.deleteById(idActividad);
    }

    @Transactional
    public void eliminarPatDeGrupo(Integer idGrupo) {
        PatGrupo pat = patGrupoRepository.findByGrupo_IdGrupo(idGrupo)
                .orElseThrow(() -> new RuntimeException("PAT no encontrado"));
        
        actividadPatGrupoRepository.deleteAll(obtenerActividadesDeGrupo(pat.getIdPatGrupo()));
        patGrupoRepository.delete(pat);
    }

    @Transactional
    public void asignarPatAutomatico(GrupoTutoria grupo) {
        patRepository.findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
                grupo.getPeriodo().getIdPeriodo(), 
                grupo.getCarrera().getIdCarrera()
        ).ifPresent(patInst -> {
            if (patGrupoRepository.findByGrupo_IdGrupo(grupo.getIdGrupo()).isEmpty()) {
                clonarPatParaGrupo(grupo.getIdGrupo(), patInst.getIdPat());
            }
        });
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