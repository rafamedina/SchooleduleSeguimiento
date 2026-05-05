package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminCentroFormDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.infrastructure.mapper.AdminCentroMapper;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCentroServiceTest {

  @Mock private CentroRepository centroRepository;
  @Mock private GrupoRepository grupoRepository;
  @Mock private AdminCentroMapper adminCentroMapper;

  @InjectMocks private AdminCentroService adminCentroService;

  private AdminCentroFormDTO buildForm(String nombre, String ubicacion) {
    AdminCentroFormDTO form = new AdminCentroFormDTO();
    form.setNombre(nombre);
    form.setUbicacion(ubicacion);
    return form;
  }

  @Test
  void crear_centroValido_persisteEntidad() {
    AdminCentroFormDTO form = buildForm("IES Ejemplo", "Calle Mayor 1");

    when(centroRepository.existsByNombre("IES Ejemplo")).thenReturn(false);

    adminCentroService.crear(form);

    verify(centroRepository).save(argThat(c -> "IES Ejemplo".equals(c.getNombre())));
  }

  @Test
  void crear_nombreExistente_lanzaIllegalArgumentException() {
    AdminCentroFormDTO form = buildForm("IES Duplicado", "Calle A");

    when(centroRepository.existsByNombre("IES Duplicado")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminCentroService.crear(form));
    verify(centroRepository, never()).save(any());
  }

  @Test
  void toggleActivo_centroConGrupos_lanzaIllegalStateException() {
    Centro centro = Centro.builder().id(1).nombre("IES Test").activo(true).build();
    when(centroRepository.findById(1)).thenReturn(Optional.of(centro));
    when(grupoRepository.existsByCentroId(1)).thenReturn(true);

    assertThatThrownBy(() -> adminCentroService.toggleActivo(1))
        .isInstanceOf(IllegalStateException.class);
    verify(centroRepository, never()).save(any());
  }

  @Test
  void toggleActivo_centroSinGrupos_invierteActivo() {
    Centro centro = Centro.builder().id(2).nombre("IES Vacio").activo(true).build();
    when(centroRepository.findById(2)).thenReturn(Optional.of(centro));
    when(grupoRepository.existsByCentroId(2)).thenReturn(false);

    adminCentroService.toggleActivo(2);

    verify(centroRepository).save(argThat(c -> Boolean.FALSE.equals(c.getActivo())));
  }

  @Test
  void toggleActivo_centroInactivo_activaDirectamente() {
    Centro centro = Centro.builder().id(3).nombre("IES Inactivo").activo(false).build();
    when(centroRepository.findById(3)).thenReturn(Optional.of(centro));

    adminCentroService.toggleActivo(3);

    verify(centroRepository).save(argThat(c -> Boolean.TRUE.equals(c.getActivo())));
    verify(grupoRepository, never()).existsByCentroId(any());
  }
}
