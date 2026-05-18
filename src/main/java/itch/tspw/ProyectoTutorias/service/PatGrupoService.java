package itch.tspw.ProyectoTutorias.service;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        
        if (!actividadesClonadas.isEmpty()) {
            actividadPatGrupoRepository.saveAll(actividadesClonadas);
        }
        
        return patGuardado;
    }

    @Transactional
    public PatGrupo sincronizarPatInstitucional(Integer idGrupo) {
        GrupoTutoria grupo = grupoService.obtenerPorId(idGrupo);
        PatInstitucional patInst = patRepository.findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
                grupo.getPeriodo().getIdPeriodo(),
                grupo.getCarrera().getIdCarrera()
        ).orElseThrow(() -> new RuntimeException("PAT institucional no encontrado para este grupo."));

        PatGrupo patGrupo = patGrupoRepository.findByGrupo_IdGrupo(idGrupo)
                .orElseGet(() -> {
                    PatGrupo nuevoPatGrupo = new PatGrupo();
                    nuevoPatGrupo.setGrupo(grupo);
                    nuevoPatGrupo.setFechaAdaptacion(LocalDate.now());
                    return nuevoPatGrupo;
                });

        patGrupo.setPatInstitucionalOrigen(patInst);
        patGrupo.setFechaAdaptacion(LocalDate.now());
        PatGrupo patGuardado = patGrupoRepository.save(patGrupo);

        List<ActividadPatGrupo> actividadesGrupo = actividadPatGrupoRepository
                .findByPatGrupo_IdPatGrupoOrderBySemanaProgramadaAsc(patGuardado.getIdPatGrupo());
        List<ActividadPatGrupo> nuevasActividades = new ArrayList<>();

        for (ActividadPat actInst : actividadPatService.listarPorPat(patInst.getIdPat())) {
            boolean yaExiste = actividadesGrupo.stream().anyMatch(actGrupo ->
                    Objects.equals(actInst.getSemanaProgramada(), actGrupo.getSemanaProgramada())
                            || actInst.getTitulo().equalsIgnoreCase(actGrupo.getTitulo())
            );

            if (!yaExiste) {
                ActividadPatGrupo actGrupo = new ActividadPatGrupo();
                actGrupo.setPatGrupo(patGuardado);
                actGrupo.setTitulo(actInst.getTitulo());
                actGrupo.setDescripcion(actInst.getDescripcion());
                actGrupo.setSemanaProgramada(actInst.getSemanaProgramada());
                actGrupo.setEstatus("Pendiente");
                nuevasActividades.add(actGrupo);
            }
        }

        if (!nuevasActividades.isEmpty()) {
            actividadPatGrupoRepository.saveAll(nuevasActividades);
        }

        return patGuardado;
    }
    
    @Transactional
    public void asignarMoldeAGrupo(Integer idGrupo, Integer idPat) {
        if (patGrupoRepository.findByGrupo_IdGrupo(idGrupo).isPresent()) {
            throw new RuntimeException("Este grupo ya cuenta con un PAT asignado.");
        }

        GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
            .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        
        PatInstitucional molde = patRepository.findById(idPat)
            .orElseThrow(() -> new RuntimeException("Molde no encontrado"));

        PatGrupo nuevoPatGrupo = new PatGrupo();
        nuevoPatGrupo.setGrupo(grupo);
        nuevoPatGrupo.setPatInstitucionalOrigen(molde);
        nuevoPatGrupo.setFechaAdaptacion(LocalDate.now());

        PatGrupo patGuardado = patGrupoRepository.save(nuevoPatGrupo);

        List<ActividadPat> actividadesBase = actividadPatService.listarPorPat(idPat);
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

        if (!actividadesClonadas.isEmpty()) {
            actividadPatGrupoRepository.saveAll(actividadesClonadas);
        }
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
        Optional<PatInstitucional> patInstOpt = patRepository.findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
                grupo.getPeriodo().getIdPeriodo(), 
                grupo.getCarrera().getIdCarrera()
        );

        if (patInstOpt.isPresent()) {
            if (patGrupoRepository.findByGrupo_IdGrupo(grupo.getIdGrupo()).isEmpty()) {
                clonarPatParaGrupo(grupo.getIdGrupo(), patInstOpt.get().getIdPat());
            }
        }
    }

    @Transactional
    public void asignarPatAGruposExistentes(PatInstitucional patInst) {
        if (patInst.getCarrera() == null) {
            return;
        }

        List<GrupoTutoria> grupos = grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(true);

        for (GrupoTutoria grupo : grupos) {
            boolean mismoPeriodo = grupo.getPeriodo().getIdPeriodo().equals(patInst.getPeriodo().getIdPeriodo());
            boolean mismaCarrera = grupo.getCarrera().getIdCarrera().equals(patInst.getCarrera().getIdCarrera());
            if (mismoPeriodo && mismaCarrera) {
                if (patGrupoRepository.findByGrupo_IdGrupo(grupo.getIdGrupo()).isEmpty()) {
                    clonarPatParaGrupo(grupo.getIdGrupo(), patInst.getIdPat());
                }
            }
        }
    }

    @Transactional
    public void sincronizarPatConGruposExistentes(PatInstitucional patInst) {
        if (patInst.getCarrera() == null) {
            return;
        }

        List<GrupoTutoria> grupos = grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(true);

        for (GrupoTutoria grupo : grupos) {
            boolean mismoPeriodo = grupo.getPeriodo().getIdPeriodo().equals(patInst.getPeriodo().getIdPeriodo());
            boolean mismaCarrera = grupo.getCarrera().getIdCarrera().equals(patInst.getCarrera().getIdCarrera());

            if (!mismoPeriodo || !mismaCarrera) {
                continue;
            }

            Optional<PatGrupo> patGrupoOpt = patGrupoRepository.findByGrupo_IdGrupo(grupo.getIdGrupo());
            if (patGrupoOpt.isEmpty()) {
                clonarPatParaGrupo(grupo.getIdGrupo(), patInst.getIdPat());
                continue;
            }

            PatGrupo patGrupo = patGrupoOpt.get();
            if (patGrupo.getPatInstitucionalOrigen() != null
                    && !patGrupo.getPatInstitucionalOrigen().getIdPat().equals(patInst.getIdPat())) {
                continue;
            }

            patGrupo.setPatInstitucionalOrigen(patInst);
            patGrupoRepository.save(patGrupo);

            List<ActividadPatGrupo> actividadesGrupo = actividadPatGrupoRepository
                    .findByPatGrupo_IdPatGrupoOrderBySemanaProgramadaAsc(patGrupo.getIdPatGrupo());
            List<ActividadPatGrupo> nuevasActividades = new ArrayList<>();

            for (ActividadPat actividadPat : actividadPatService.listarPorPat(patInst.getIdPat())) {
                boolean yaExiste = actividadesGrupo.stream().anyMatch(actividadGrupo ->
                        Objects.equals(actividadPat.getSemanaProgramada(), actividadGrupo.getSemanaProgramada())
                                || actividadPat.getTitulo().equalsIgnoreCase(actividadGrupo.getTitulo())
                );

                if (!yaExiste) {
                    ActividadPatGrupo actividadGrupo = new ActividadPatGrupo();
                    actividadGrupo.setPatGrupo(patGrupo);
                    actividadGrupo.setTitulo(actividadPat.getTitulo());
                    actividadGrupo.setDescripcion(actividadPat.getDescripcion());
                    actividadGrupo.setSemanaProgramada(actividadPat.getSemanaProgramada());
                    actividadGrupo.setEstatus("Pendiente");
                    nuevasActividades.add(actividadGrupo);
                }
            }

            if (!nuevasActividades.isEmpty()) {
                actividadPatGrupoRepository.saveAll(nuevasActividades);
            }
        }
    }

    @Transactional
    public void asignarPatMoldeAGruposDelPeriodo(PatInstitucional patInst) {
        if (patInst.getCarrera() == null) {
            return;
        }

        List<GrupoTutoria> grupos = grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(true);

        for (GrupoTutoria grupo : grupos) {
            boolean mismoPeriodo = grupo.getPeriodo().getIdPeriodo().equals(patInst.getPeriodo().getIdPeriodo());
            boolean mismaCarrera = grupo.getCarrera().getIdCarrera().equals(patInst.getCarrera().getIdCarrera());
            if (mismoPeriodo && mismaCarrera) {
                if (patGrupoRepository.findByGrupo_IdGrupo(grupo.getIdGrupo()).isEmpty()) {
                    asignarMoldeAGrupo(grupo.getIdGrupo(), patInst.getIdPat());
                }
            }
        }
    }
}
