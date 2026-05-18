package itch.tspw.ProyectoTutorias.controller;

import java.time.LocalDate;
import java.util.*;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tspw.ProyectoTutorias.model.*;
import itch.tspw.ProyectoTutorias.repository.*;
import itch.tspw.ProyectoTutorias.service.*;

@Controller
@RequestMapping("/coordinador")
public class CoordinadorController {

	private final EvidenciaService evidenciaService;
	private final PeriodoEscolarService periodoEscolarService;
	private final ReporteService reporteService;
	private final GrupoTutoriaService grupoTutoriaService;
	private final SesionService sesionService;
	private final PatGrupoService patGrupoService;
	private final SesionRepository sesionRepository;

	private final TutorRepository tutorRepository;
	private final EstudianteRepository estudianteRepository;
	private final GrupoTutoriaRepository grupoTutoriaRepository;
	private final PatRepository patRepository;
	private final UsuarioRepository usuarioRepository;
	private final AsistenciaRepository asistenciaRepository;
	private final NecesidadesRepository necesidadRepository;
	private final ActividadPatRepository actividadPatRepository;

	public CoordinadorController(EvidenciaService evidenciaService, ReporteService reporteService,
			PeriodoEscolarService periodoEscolarService, GrupoTutoriaService grupoTutoriaService,
			SesionService sesionService, PatGrupoService patGrupoService, TutorRepository tutorRepository,
			EstudianteRepository estudianteRepository, GrupoTutoriaRepository grupoTutoriaRepository,
			PatRepository patRepository, UsuarioRepository usuarioRepository, AsistenciaRepository asistenciaRepository,
			NecesidadesRepository necesidadRepository, SesionRepository sesionRepository,
			ActividadPatRepository actividadPatRepository) {

		this.evidenciaService = evidenciaService;
		this.reporteService = reporteService;
		this.periodoEscolarService = periodoEscolarService;
		this.grupoTutoriaService = grupoTutoriaService;
		this.sesionService = sesionService;
		this.patGrupoService = patGrupoService;
		this.tutorRepository = tutorRepository;
		this.estudianteRepository = estudianteRepository;
		this.grupoTutoriaRepository = grupoTutoriaRepository;
		this.patRepository = patRepository;
		this.usuarioRepository = usuarioRepository;
		this.asistenciaRepository = asistenciaRepository;
		this.necesidadRepository = necesidadRepository;
		this.sesionRepository = sesionRepository;
		this.actividadPatRepository = actividadPatRepository;
	}

	@GetMapping("/panel")
	public String cargarDashboardCoordinador(Model model) {
		List<GrupoTutoria> gruposActivos = grupoTutoriaRepository.findByPeriodo_EstatusActivoAndActivoTrue(true);
		model.addAttribute("gruposActivos", gruposActivos);
		model.addAttribute("totalGruposActivos", gruposActivos.size());
		model.addAttribute("evidenciasPendientes", evidenciaService.obtenerEvidenciasPendientes());
		model.addAttribute("alumnosSinGrupo", estudianteRepository.findByGrupoIsNullAndActivoTrue().size());
		model.addAttribute("totalTutores", tutorRepository.count());
		model.addAttribute("alertasAcademicas", 0);
		return "coordinador/dashboard-coordinador";
	}

	@GetMapping("/busquedas")
	public String cargarCentroDeBusquedas(Model model) {
		model.addAttribute("periodos", periodoEscolarService.listarTodos());
		model.addAttribute("estudiantes", estudianteRepository.findByActivoTrue());
		return "coordinador/busquedas";
	}

	@GetMapping("/buscar-tutores")
	public String buscarTutoresPorSemestre(@RequestParam("idPeriodo") Integer idPeriodo, Model model) {
		model.addAttribute("periodo", periodoEscolarService.obtenerPorId(idPeriodo));
		model.addAttribute("resultados", grupoTutoriaService.buscarTutoriasPorPeriodo(idPeriodo));
		model.addAttribute("periodoId", idPeriodo);
		return "coordinador/resultados-tutores";
	}

	@GetMapping("/buscar-historial-alumno")
	public String buscarHistorialAlumno(@RequestParam("idEstudiante") Integer idEstudiante, Model model) {
		estudianteRepository.findById(idEstudiante)
				.ifPresent(estudiante -> model.addAttribute("estudiante", estudiante));
		model.addAttribute("resultados", grupoTutoriaService.buscarHistorialTutoriasDeEstudiante(idEstudiante));
		model.addAttribute("estudianteId", idEstudiante);
		return "coordinador/resultados-historial";
	}

	@GetMapping("/buscar-actividades")
	public String buscarActividadesPorFecha(@RequestParam("fecha") LocalDate fecha, Model model) {
		model.addAttribute("resultados", sesionService.buscarActividadesPorFecha(fecha));
		model.addAttribute("fechaBusqueda", fecha);
		return "coordinador/resultados-actividades";
	}

	@GetMapping("/reportes")
	public String centroDeReportes(Model model) {
		model.addAttribute("grupos", grupoTutoriaRepository.findAll());
		model.addAttribute("patsInstitucionales", patRepository.findByCarreraIsNotNullAndActivoTrueOrderByIdPatDesc());
		return "coordinador/reportes";
	}

	@GetMapping("/reportes/grupos/pdf")
	public ResponseEntity<byte[]> descargarPdfGrupos() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("grupos", grupoTutoriaRepository.findAll());
		variables.put("fechaImpresion", LocalDate.now());
		byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-grupos", variables);
		return crearResponseEntityPdf(pdfBytes, "Listado_Grupos_Tutorias_ITCH.pdf");
	}

	@GetMapping("/reportes/grupo-detalle/pdf")
	public ResponseEntity<byte[]> descargarPdfGrupoAlumnos(@RequestParam("idGrupo") Integer idGrupo) {
		GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
				.orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));

		Map<String, Object> variables = new HashMap<>();
		variables.put("grupo", grupo);
		variables.put("estudiantes", grupo.getEstudiantes());
		variables.put("fechaImpresion", LocalDate.now());

		byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-grupo-alumnos", variables);
		return crearResponseEntityPdf(pdfBytes, "Lista_Alumnos_Grupo_" + grupo.getNombreGrupo() + ".pdf");
	}

	@GetMapping("/reportes/tutores/pdf")
	public ResponseEntity<byte[]> descargarPdfTutores() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("tutores", tutorRepository.findAll());
		variables.put("fechaImpresion", LocalDate.now());

		byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-tutores", variables);
		return crearResponseEntityPdf(pdfBytes, "Plantilla_Docente_Tutorias_ITCH.pdf");
	}

	@GetMapping("/reportes/pats/pdf")
	public ResponseEntity<byte[]> descargarPdfPats() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("pats", patRepository.findByActivoTrueOrderByIdPatDesc());
		variables.put("fechaImpresion", LocalDate.now());

		byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-pats", variables);
		return crearResponseEntityPdf(pdfBytes, "Catalogo_PATs_ITCH.pdf");
	}

	@GetMapping("/reportes/pat-detalle/pdf")
	public ResponseEntity<byte[]> descargarPdfPatDetalle(@RequestParam("idPat") Integer idPat) {
		PatInstitucional pat = patRepository.findById(idPat)
				.orElseThrow(() -> new IllegalArgumentException("PAT no encontrado: " + idPat));

		Map<String, Object> variables = new HashMap<>();
		variables.put("pat", pat);
		variables.put("actividades", actividadPatRepository.findByPat_IdPat(idPat));
		variables.put("fechaImpresion", LocalDate.now());

		byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-pat-cronograma", variables);
		return crearResponseEntityPdf(pdfBytes, "Cronograma_PAT_" + pat.getVersion().replace(" ", "_") + ".pdf");
	}

	@GetMapping("/reportes/constancias/pdf")
	public ResponseEntity<byte[]> descargarConstanciasLib(@RequestParam Integer idGrupo) {
		GrupoTutoria grupo = grupoTutoriaRepository.findById(idGrupo)
				.orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));

		List<Estudiante> acreditados = new ArrayList<>();
		for (Estudiante e : grupo.getEstudiantes()) {
			List<Asistencia> asis = asistenciaRepository.findByEstudiante_IdEstudiante(e.getIdEstudiante());
			if (!asis.isEmpty()) {
				long presentes = asis.stream().filter(a -> Boolean.TRUE.equals(a.getPresente())).count();
				if ((presentes * 100 / asis.size()) >= 80)
					acreditados.add(e);
			}
		}

		Map<String, Object> vars = new HashMap<>();
		vars.put("grupo", grupo);
		vars.put("alumnos", acreditados);
		vars.put("fechaImpresion", LocalDate.now());

		byte[] pdfBytes = reporteService.generarPdfDesdeHtml("pdf/reporte-constancias", vars);
		return crearResponseEntityPdf(pdfBytes, "Constancias_Liberacion_" + grupo.getNombreGrupo() + ".pdf");
	}

	@GetMapping("/necesidades")
	public String verTodasNecesidades(Model model) {
		model.addAttribute("necesidades", necesidadRepository.findAllByOrderByFechaSolicitudDesc());
		return "coordinador/necesidades-lista";
	}

	private ResponseEntity<byte[]> crearResponseEntityPdf(byte[] content, String filename) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}

	@GetMapping("/perfil")
	public String obtenerPerfilCoordinador(Model model, Authentication authentication) {
		Usuario usuario = usuarioRepository.findByCorreoInstitucional(authentication.getName())
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		model.addAttribute("usuario", usuario);
		model.addAttribute("totalTutores", tutorRepository.count());
		model.addAttribute("totalEstudiantes", estudianteRepository.findByActivoTrue().size());
		return "coordinador/perfil";
	}

	@GetMapping("/grupo/{id}/detalle")
	public String verDetalleGrupo(@PathVariable("id") Integer id, Model model) {
	    GrupoTutoria grupo = grupoTutoriaRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + id));

	    List<Sesion> sesiones = sesionRepository.findByGrupo_IdGrupo(id);

	    PatGrupo patGrupo = patGrupoService.obtenerPatDeGrupo(id);
	    if (patGrupo != null) {
	        model.addAttribute("actividadesGrupo", patGrupoService.obtenerActividadesDeGrupo(patGrupo.getIdPatGrupo()));
	    }

	    patRepository.findFirstByPeriodo_IdPeriodoAndCarrera_IdCarreraAndActivoTrueOrderByIdPatDesc(
	            grupo.getPeriodo().getIdPeriodo(),
	            grupo.getCarrera().getIdCarrera()
	    ).ifPresent(pat -> model.addAttribute("patInstitucional", pat));

	    model.addAttribute("grupo", grupo);
	    model.addAttribute("sesiones", sesiones);
	    model.addAttribute("totalEstudiantes", grupo.getEstudiantes().size());

	    return "coordinador/detalle-grupo";
	}
}
