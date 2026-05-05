package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminMatriculaFormDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.Grupo;
import com.tfg.schooledule.domain.entity.Imparticion;
import com.tfg.schooledule.domain.entity.Matricula;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.CalificacionRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminAlumnoServiceTest {

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private MatriculaRepository matriculaRepository;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private CalificacionRepository calificacionRepository;

  @InjectMocks private AdminAlumnoService adminAlumnoService;

  private Usuario alumnoConRol() {
    Rol rol = Rol.builder().id(1).nombre("ALUMNO").build();
    return Usuario.builder()
        .id(1)
        .nombre("Juan")
        .apellidos("García")
        .email("juan@test.com")
        .roles(Set.of(rol))
        .build();
  }

  private Imparticion imparticion() {
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    Grupo grupo = Grupo.builder().id(1).nombre("1DAW-A").build();
    Modulo modulo = Modulo.builder().id(1).codigo("DAW01").nombre("Programación").build();
    return Imparticion.builder().id(1).centro(centro).grupo(grupo).modulo(modulo).build();
  }

  @Test
  void crearMatricula_datosValidos_persisteEntidad() {
    AdminMatriculaFormDTO form = new AdminMatriculaFormDTO(null, 1, EstadoMatricula.ACTIVA, false);
    Usuario alumno = alumnoConRol();
    Imparticion imp = imparticion();

    when(usuarioRepository.findById(1)).thenReturn(Optional.of(alumno));
    when(imparticionRepository.findById(1)).thenReturn(Optional.of(imp));
    when(matriculaRepository.existsByAlumnoIdAndImparticionId(1, 1)).thenReturn(false);

    adminAlumnoService.crearMatricula(1, form);

    verify(matriculaRepository).save(argThat(m -> m.getAlumno().getId().equals(1)));
  }

  @Test
  void crearMatricula_alumnoYaMatriculado_lanzaIllegalArgumentException() {
    AdminMatriculaFormDTO form = new AdminMatriculaFormDTO(null, 1, EstadoMatricula.ACTIVA, false);
    Usuario alumno = alumnoConRol();
    Imparticion imp = imparticion();

    when(usuarioRepository.findById(1)).thenReturn(Optional.of(alumno));
    when(imparticionRepository.findById(1)).thenReturn(Optional.of(imp));
    when(matriculaRepository.existsByAlumnoIdAndImparticionId(1, 1)).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminAlumnoService.crearMatricula(1, form));
    verify(matriculaRepository, never()).save(any());
  }

  @Test
  void eliminarMatricula_conCalificaciones_lanzaIllegalStateException() {
    when(matriculaRepository.existsById(1)).thenReturn(true);
    when(calificacionRepository.existsByMatriculaId(1)).thenReturn(true);

    assertThatThrownBy(() -> adminAlumnoService.eliminarMatricula(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("calificaciones");
    verify(matriculaRepository, never()).deleteById(any());
  }

  @Test
  void eliminarMatricula_sinCalificaciones_eliminaCorrectamente() {
    when(matriculaRepository.existsById(2)).thenReturn(true);
    when(calificacionRepository.existsByMatriculaId(2)).thenReturn(false);

    adminAlumnoService.eliminarMatricula(2);

    verify(matriculaRepository).deleteById(2);
  }

  @Test
  void actualizarMatricula_cambiandoEstado_guarda() {
    AdminMatriculaFormDTO form = new AdminMatriculaFormDTO(1, 1, EstadoMatricula.BAJA, false);
    Matricula matricula =
        Matricula.builder().id(1).estado(EstadoMatricula.ACTIVA).esRepetidor(false).build();

    when(matriculaRepository.findById(1)).thenReturn(Optional.of(matricula));

    adminAlumnoService.actualizarMatricula(1, form);

    verify(matriculaRepository).save(argThat(m -> EstadoMatricula.BAJA.equals(m.getEstado())));
  }
}
