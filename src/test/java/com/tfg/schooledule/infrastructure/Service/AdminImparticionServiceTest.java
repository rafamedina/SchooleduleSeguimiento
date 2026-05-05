package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminImparticionFormDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.mapper.AdminImparticionMapper;
import com.tfg.schooledule.infrastructure.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminImparticionServiceTest {

  @Mock private ImparticionRepository imparticionRepository;
  @Mock private ModuloRepository moduloRepository;
  @Mock private GrupoRepository grupoRepository;
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private CentroRepository centroRepository;
  @Mock private MatriculaRepository matriculaRepository;
  @Mock private PeriodoEvaluacionRepository periodoEvaluacionRepository;
  @Mock private AdminImparticionMapper adminImparticionMapper;

  @InjectMocks private AdminImparticionService adminImparticionService;

  private AdminImparticionFormDTO buildForm(
      int moduloId, int grupoId, int profesorId, int centroId) {
    AdminImparticionFormDTO form = new AdminImparticionFormDTO();
    form.setModuloId(moduloId);
    form.setGrupoId(grupoId);
    form.setProfesorId(profesorId);
    form.setCentroId(centroId);
    return form;
  }

  @Test
  void crear_imparticionValida_guardaEntidad() {
    AdminImparticionFormDTO form = buildForm(1, 1, 2, 1);
    Modulo modulo = Modulo.builder().id(1).codigo("DAW01").nombre("Programación").build();
    Grupo grupo = Grupo.builder().id(1).nombre("1DAW-A").build();
    Usuario profesor = Usuario.builder().id(2).nombre("Ana").apellidos("López").build();
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();

    when(imparticionRepository.existsByModuloIdAndGrupoId(1, 1)).thenReturn(false);
    when(moduloRepository.findById(1)).thenReturn(Optional.of(modulo));
    when(grupoRepository.findById(1)).thenReturn(Optional.of(grupo));
    when(usuarioRepository.findById(2)).thenReturn(Optional.of(profesor));
    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));

    adminImparticionService.crear(form);

    verify(imparticionRepository).save(argThat(i -> i.getModulo().getId().equals(1)));
  }

  @Test
  void crear_moduloGrupoDuplicado_lanzaIllegalArgumentException() {
    AdminImparticionFormDTO form = buildForm(1, 1, 2, 1);

    when(imparticionRepository.existsByModuloIdAndGrupoId(1, 1)).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminImparticionService.crear(form));
    verify(imparticionRepository, never()).save(any());
  }

  @Test
  void eliminar_sinMatriculasNiPeriodos_invocaDelete() {
    when(imparticionRepository.existsById(1)).thenReturn(true);
    when(matriculaRepository.existsByImparticionId(1)).thenReturn(false);
    when(periodoEvaluacionRepository.existsByImparticionId(1)).thenReturn(false);

    adminImparticionService.eliminar(1);

    verify(imparticionRepository).deleteById(1);
  }

  @Test
  void eliminar_conMatriculas_lanzaIllegalStateException() {
    when(imparticionRepository.existsById(1)).thenReturn(true);
    when(matriculaRepository.existsByImparticionId(1)).thenReturn(true);

    assertThatThrownBy(() -> adminImparticionService.eliminar(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("matrículas");
    verify(imparticionRepository, never()).deleteById(any());
  }

  @Test
  void eliminar_conPeriodos_lanzaIllegalStateException() {
    when(imparticionRepository.existsById(1)).thenReturn(true);
    when(matriculaRepository.existsByImparticionId(1)).thenReturn(false);
    when(periodoEvaluacionRepository.existsByImparticionId(1)).thenReturn(true);

    assertThatThrownBy(() -> adminImparticionService.eliminar(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("periodos");
    verify(imparticionRepository, never()).deleteById(any());
  }
}
