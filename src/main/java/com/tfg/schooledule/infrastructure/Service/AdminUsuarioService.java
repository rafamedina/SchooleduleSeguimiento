package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.domain.dto.AdminUsuarioListDTO;
import com.tfg.schooledule.domain.dto.DashboardStatsDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.mapper.AdminUsuarioMapper;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUsuarioService {

  private static final String ERR_USUARIO = "Usuario no encontrado: ";
  private static final String ERR_EN_USO = "' ya está en uso";

  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final CentroRepository centroRepository;
  private final CursoAcademicoRepository cursoAcademicoRepository;
  private final AdminUsuarioMapper adminUsuarioMapper;
  private final PasswordEncoder passwordEncoder;
  private final MatriculaRepository matriculaRepository;
  private final ImparticionRepository imparticionRepository;

  public AdminUsuarioService(
      UsuarioRepository usuarioRepository,
      RolRepository rolRepository,
      CentroRepository centroRepository,
      CursoAcademicoRepository cursoAcademicoRepository,
      AdminUsuarioMapper adminUsuarioMapper,
      PasswordEncoder passwordEncoder,
      MatriculaRepository matriculaRepository,
      ImparticionRepository imparticionRepository) {
    this.usuarioRepository = usuarioRepository;
    this.rolRepository = rolRepository;
    this.centroRepository = centroRepository;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
    this.adminUsuarioMapper = adminUsuarioMapper;
    this.passwordEncoder = passwordEncoder;
    this.matriculaRepository = matriculaRepository;
    this.imparticionRepository = imparticionRepository;
  }

  @Transactional(readOnly = true)
  public List<AdminUsuarioListDTO> listarTodos() {
    return usuarioRepository.findAllByOrderByApellidosAscNombreAsc().stream()
        .map(adminUsuarioMapper::toListDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AdminUsuarioListDTO> listarFiltrado(String rolNombre) {
    if (rolNombre == null || rolNombre.isBlank()) {
      return usuarioRepository.findAllByOrderByApellidosAscNombreAsc().stream()
          .map(adminUsuarioMapper::toListDTO)
          .toList();
    }
    return usuarioRepository.findByRol(rolNombre).stream()
        .map(adminUsuarioMapper::toListDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminUsuarioFormDTO obtenerParaEditar(Integer id) {
    Usuario usuario =
        usuarioRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ERR_USUARIO + id));
    return adminUsuarioMapper.toFormDTO(usuario);
  }

  @Transactional
  public void crear(AdminUsuarioFormDTO dto) {
    if (dto.getPassword() == null || dto.getPassword().isBlank()) {
      throw new IllegalArgumentException("La contraseña es obligatoria para nuevos usuarios");
    }
    if (usuarioRepository.existsByUsername(dto.getUsername())) {
      throw new IllegalArgumentException("El username '" + dto.getUsername() + ERR_EN_USO);
    }
    if (usuarioRepository.existsByEmail(dto.getEmail())) {
      throw new IllegalArgumentException("El email '" + dto.getEmail() + ERR_EN_USO);
    }

    List<Rol> roles = rolRepository.findAllById(dto.getRoleIds());
    List<Centro> centros = centroRepository.findAllById(dto.getCentroIds());

    Usuario usuario =
        Usuario.builder()
            .username(dto.getUsername())
            .nombre(dto.getNombre())
            .apellidos(dto.getApellidos())
            .email(dto.getEmail())
            .passwordHash(passwordEncoder.encode(dto.getPassword()))
            .activo(true)
            .roles(new HashSet<>(roles))
            .centros(new HashSet<>(centros))
            .build();

    usuarioRepository.save(usuario);
  }

  @Transactional
  public void actualizar(Integer id, AdminUsuarioFormDTO dto) {
    Usuario usuario =
        usuarioRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ERR_USUARIO + id));

    if (usuarioRepository.existsByUsernameAndIdNot(dto.getUsername(), id)) {
      throw new IllegalArgumentException("El username '" + dto.getUsername() + ERR_EN_USO);
    }
    if (usuarioRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
      throw new IllegalArgumentException("El email '" + dto.getEmail() + ERR_EN_USO);
    }

    usuario.setUsername(dto.getUsername());
    usuario.setNombre(dto.getNombre());
    usuario.setApellidos(dto.getApellidos());
    usuario.setEmail(dto.getEmail());

    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
    }

    List<Rol> roles = rolRepository.findAllById(dto.getRoleIds());
    List<Centro> centros = centroRepository.findAllById(dto.getCentroIds());
    usuario.setRoles(new HashSet<>(roles));
    usuario.setCentros(new HashSet<>(centros));

    usuarioRepository.save(usuario);
  }

  @Transactional
  public void toggleActivo(Integer id) {
    Usuario usuario =
        usuarioRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ERR_USUARIO + id));
    boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre().contains("ADMIN"));
    if (Boolean.TRUE.equals(usuario.getActivo()) && esAdmin) {
      long adminsActivos = usuarioRepository.countAdminsActivos();
      if (adminsActivos <= 1) {
        throw new IllegalStateException(
            "No se puede desactivar el único administrador activo del sistema");
      }
    }
    usuario.setActivo(!usuario.getActivo());
    usuarioRepository.save(usuario);
  }

  @Transactional(readOnly = true)
  public DashboardStatsDTO getStats() {
    List<Usuario> todos = usuarioRepository.findAll();
    long activos = todos.stream().filter(u -> Boolean.TRUE.equals(u.getActivo())).count();
    long inactivos = todos.size() - activos;
    long admins =
        todos.stream()
            .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre().contains("ADMIN")))
            .count();
    long alumnos =
        todos.stream()
            .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre().contains("ALUMNO")))
            .count();
    long profesores =
        todos.stream()
            .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre().contains("PROFESOR")))
            .count();
    long totalCentros = centroRepository.count();
    String cursoActivo =
        cursoAcademicoRepository.findByActivo(true).map(c -> c.getNombre()).orElse(null);
    long totalMatriculasActivas = matriculaRepository.countMatriculasActivas();
    long totalImparticiones = imparticionRepository.count();
    return new DashboardStatsDTO(
        todos.size(),
        activos,
        inactivos,
        admins,
        alumnos,
        profesores,
        totalCentros,
        cursoActivo,
        totalMatriculasActivas,
        totalImparticiones);
  }
}
