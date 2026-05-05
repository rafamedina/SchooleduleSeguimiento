package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminModuloFormDTO;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.infrastructure.mapper.AdminModuloMapper;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminModuloServiceTest {

  @Mock private ModuloRepository moduloRepository;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private ResultadoAprendizajeRepository raRepository;
  @Mock private AdminModuloMapper adminModuloMapper;

  @InjectMocks private AdminModuloService adminModuloService;

  private AdminModuloFormDTO buildForm(String codigo, String nombre) {
    AdminModuloFormDTO form = new AdminModuloFormDTO();
    form.setCodigo(codigo);
    form.setNombre(nombre);
    return form;
  }

  @Test
  void crear_moduloValido_persisteEntidad() {
    AdminModuloFormDTO form = buildForm("DAW01", "Desarrollo Web en Entorno Cliente");

    when(moduloRepository.existsByCodigo("DAW01")).thenReturn(false);

    adminModuloService.crear(form);

    verify(moduloRepository).save(argThat(m -> "DAW01".equals(m.getCodigo())));
  }

  @Test
  void crear_codigoExistente_lanzaIllegalArgumentException() {
    AdminModuloFormDTO form = buildForm("DAW01", "Duplicado");

    when(moduloRepository.existsByCodigo("DAW01")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminModuloService.crear(form));
    verify(moduloRepository, never()).save(any());
  }

  @Test
  void toggleActivo_conImparticiones_lanzaIllegalStateException() {
    Modulo modulo = Modulo.builder().id(1).codigo("DAW01").nombre("Test").activo(true).build();
    when(moduloRepository.findById(1)).thenReturn(Optional.of(modulo));
    when(imparticionRepository.existsByModuloId(1)).thenReturn(true);

    assertThatThrownBy(() -> adminModuloService.toggleActivo(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("imparticiones");
    verify(moduloRepository, never()).save(any());
  }

  @Test
  void toggleActivo_conResultadosAprendizaje_lanzaIllegalStateException() {
    Modulo modulo = Modulo.builder().id(2).codigo("DAW02").nombre("Test").activo(true).build();
    when(moduloRepository.findById(2)).thenReturn(Optional.of(modulo));
    when(imparticionRepository.existsByModuloId(2)).thenReturn(false);
    when(raRepository.existsByModuloId(2)).thenReturn(true);

    assertThatThrownBy(() -> adminModuloService.toggleActivo(2))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("resultados de aprendizaje");
    verify(moduloRepository, never()).save(any());
  }

  @Test
  void toggleActivo_sinDependencias_invierteActivo() {
    Modulo modulo = Modulo.builder().id(3).codigo("DAW03").nombre("Test").activo(true).build();
    when(moduloRepository.findById(3)).thenReturn(Optional.of(modulo));
    when(imparticionRepository.existsByModuloId(3)).thenReturn(false);
    when(raRepository.existsByModuloId(3)).thenReturn(false);

    adminModuloService.toggleActivo(3);

    verify(moduloRepository).save(argThat(m -> Boolean.FALSE.equals(m.getActivo())));
  }

  @Test
  void toggleActivo_moduloInactivo_activaDirectamente() {
    Modulo modulo = Modulo.builder().id(4).codigo("DAW04").nombre("Test").activo(false).build();
    when(moduloRepository.findById(4)).thenReturn(Optional.of(modulo));

    adminModuloService.toggleActivo(4);

    verify(moduloRepository).save(argThat(m -> Boolean.TRUE.equals(m.getActivo())));
    verify(imparticionRepository, never()).existsByModuloId(any());
  }
}
