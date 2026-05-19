package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.UsuarioImportErrorDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportPreviewDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportResultado;
import com.tfg.schooledule.domain.dto.UsuarioImportRowDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.domain.entity.Grupo;
import com.tfg.schooledule.domain.entity.Imparticion;
import com.tfg.schooledule.domain.entity.Matricula;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.domain.exception.UsuarioImportException;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioImportService {

  private final UsuarioExcelParserService parser;
  private final UsuarioImportValidatorService validator;
  private final CentroRepository centroRepository;
  private final CursoAcademicoRepository cursoAcademicoRepository;
  private final GrupoRepository grupoRepository;
  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final ImparticionRepository imparticionRepository;
  private final MatriculaRepository matriculaRepository;
  private final PasswordEncoder passwordEncoder;

  public UsuarioImportService(
      UsuarioExcelParserService parser,
      UsuarioImportValidatorService validator,
      CentroRepository centroRepository,
      CursoAcademicoRepository cursoAcademicoRepository,
      GrupoRepository grupoRepository,
      UsuarioRepository usuarioRepository,
      RolRepository rolRepository,
      ImparticionRepository imparticionRepository,
      MatriculaRepository matriculaRepository,
      PasswordEncoder passwordEncoder) {
    this.parser = parser;
    this.validator = validator;
    this.centroRepository = centroRepository;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
    this.grupoRepository = grupoRepository;
    this.usuarioRepository = usuarioRepository;
    this.rolRepository = rolRepository;
    this.imparticionRepository = imparticionRepository;
    this.matriculaRepository = matriculaRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UsuarioImportResultado importar(byte[] bytes) {
    List<UsuarioImportRowDTO> filas = parser.parsear(bytes);
    UsuarioImportPreviewDTO preview = validator.validar(filas);
    if (!preview.valido()) {
      throw new UsuarioImportException(preview.errores());
    }

    List<UsuarioImportErrorDTO> erroresBD = new ArrayList<>();
    List<FilaResuelta> filasResueltas = new ArrayList<>();

    for (UsuarioImportRowDTO fila : filas) {
      resolverFila(fila, erroresBD, filasResueltas);
    }

    if (!erroresBD.isEmpty()) {
      throw new UsuarioImportException(erroresBD);
    }

    Rol rolAlumno = rolRepository.findByNombre("ROLE_ALUMNO");
    int usuariosCreados = 0;
    int matriculasCreadas = 0;

    for (FilaResuelta fr : filasResueltas) {
      Usuario usuario = crearUsuario(fr, rolAlumno);
      usuariosCreados++;
      matriculasCreadas += matricularEnGrupo(usuario, fr.grupo(), fr.fila().esRepetidorRaw());
    }

    return new UsuarioImportResultado(usuariosCreados, matriculasCreadas);
  }

  private void resolverFila(
      UsuarioImportRowDTO fila,
      List<UsuarioImportErrorDTO> errores,
      List<FilaResuelta> filasResueltas) {
    int f = fila.numeroFila();
    boolean filaOk = true;

    Optional<Centro> centroOpt = centroRepository.findByNombreIgnoreCase(fila.centroNombre());
    if (centroOpt.isEmpty()) {
      errores.add(
          new UsuarioImportErrorDTO(
              f, "centro_nombre", "Centro '" + fila.centroNombre() + "' no encontrado"));
      filaOk = false;
    }

    Optional<CursoAcademico> cursoOpt =
        cursoAcademicoRepository.findByNombreIgnoreCase(fila.cursoAcademicoNombre());
    if (cursoOpt.isEmpty()) {
      errores.add(
          new UsuarioImportErrorDTO(
              f,
              "curso_academico_nombre",
              "Curso académico '" + fila.cursoAcademicoNombre() + "' no encontrado"));
      filaOk = false;
    }

    Optional<Grupo> grupoOpt = Optional.empty();
    if (centroOpt.isPresent() && cursoOpt.isPresent()) {
      grupoOpt =
          grupoRepository.findByNombreIgnoreCaseAndCentroIdAndCursoAcademicoId(
              fila.grupoNombre(), centroOpt.get().getId(), cursoOpt.get().getId());
      if (grupoOpt.isEmpty()) {
        errores.add(
            new UsuarioImportErrorDTO(
                f,
                "grupo_nombre",
                "Grupo '" + fila.grupoNombre() + "' no encontrado en el centro/curso indicado"));
        filaOk = false;
      }
    }

    if (usuarioRepository.existsByUsername(fila.username())) {
      errores.add(
          new UsuarioImportErrorDTO(
              f,
              "username",
              "El username '" + fila.username() + "' ya está registrado en el sistema"));
      filaOk = false;
    }

    if (fila.email() != null
        && !fila.email().isBlank()
        && usuarioRepository.existsByEmail(fila.email())) {
      errores.add(
          new UsuarioImportErrorDTO(
              f, "email", "El email '" + fila.email() + "' ya está registrado en el sistema"));
      filaOk = false;
    }

    if (filaOk) {
      filasResueltas.add(new FilaResuelta(fila, centroOpt.get(), cursoOpt.get(), grupoOpt.get()));
    }
  }

  private Usuario crearUsuario(FilaResuelta fr, Rol rolAlumno) {
    Set<Rol> roles = new HashSet<>();
    roles.add(rolAlumno);

    Usuario usuario =
        Usuario.builder()
            .username(fr.fila().username())
            .nombre(fr.fila().nombre())
            .apellidos(fr.fila().apellidos())
            .email(fr.fila().email())
            .passwordHash(passwordEncoder.encode(fr.fila().password()))
            .activo(true)
            .mustChangePassword(true)
            .roles(roles)
            .build();

    return usuarioRepository.save(usuario);
  }

  private int matricularEnGrupo(Usuario usuario, Grupo grupo, String esRepetidorRaw) {
    List<Imparticion> imparticiones = imparticionRepository.findByGrupoId(grupo.getId());
    int count = 0;
    boolean repetidor = interpretarRepetidor(esRepetidorRaw);
    for (Imparticion imp : imparticiones) {
      if (!matriculaRepository.existsByAlumnoIdAndImparticionId(usuario.getId(), imp.getId())) {
        matriculaRepository.save(
            Matricula.builder()
                .alumno(usuario)
                .imparticion(imp)
                .centro(grupo.getCentro())
                .esRepetidor(repetidor)
                .estado(EstadoMatricula.ACTIVA)
                .build());
        count++;
      }
    }
    return count;
  }

  private boolean interpretarRepetidor(String raw) {
    if (raw == null) return false;
    String limpio = raw.trim().toLowerCase();
    return limpio.equals("si") || limpio.equals("sí") || limpio.equals("true");
  }

  private record FilaResuelta(
      UsuarioImportRowDTO fila, Centro centro, CursoAcademico curso, Grupo grupo) {}
}
