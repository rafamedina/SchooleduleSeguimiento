package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminImparticionFormDTO;
import com.tfg.schooledule.domain.dto.AdminImparticionListDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.*;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CentroAdminImparticionServiceTest {

  @Mock private CentroAdminContextService context;
  @Mock private AdminImparticionService adminImparticionService;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private GrupoRepository grupoRepository;

  @InjectMocks private CentroAdminImparticionService service;

  private static final int ADMIN_ID = 10;
  private static final int CENTRO_A = 1;

  private Centro centroA;
  private Grupo grupo;
  private Modulo modulo;
  private Imparticion imp;

  @BeforeEach
  void setUp() {
    centroA = new Centro();
    centroA.setId(CENTRO_A);
    centroA.setNombre("IES Getafe");

    grupo = new Grupo();
    grupo.setId(5);
    grupo.setNombre("1DAW-A");
    grupo.setCentro(centroA);

    modulo = new Modulo();
    modulo.setId(1);
    modulo.setCodigo("0491");
    modulo.setNombre("Sistemas de Gestión Empresarial");

    Usuario profesor = new Usuario();
    profesor.setId(3);
    profesor.setNombre("Luis");
    profesor.setApellidos("Pérez");

    imp = new Imparticion();
    imp.setId(7);
    imp.setModulo(modulo);
    imp.setGrupo(grupo);
    imp.setProfesor(profesor);
    imp.setCentro(centroA);
  }

  @Test
  void listarImparticiones_retornaFiltradas() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(imparticionRepository.findByCentroIdInOrderByGrupoNombreAscModuloNombreAsc(
            anyCollection()))
        .thenReturn(List.of(imp));

    List<AdminImparticionListDTO> result = service.listarImparticionesDeCentros(ADMIN_ID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).grupoNombre()).isEqualTo("1DAW-A");
  }

  @Test
  void obtenerParaEditar_ok_siPertenece() {
    doNothing().when(context).validateImparticionBelongsToCentroAdmin(ADMIN_ID, 7);
    var dto = new AdminImparticionFormDTO();
    when(adminImparticionService.obtenerParaEditar(7)).thenReturn(dto);

    AdminImparticionFormDTO result = service.obtenerParaEditar(ADMIN_ID, 7);

    assertThat(result).isSameAs(dto);
  }

  @Test
  void obtenerParaEditar_lanza_siEsDeOtroCentro() {
    doThrow(new AccessDeniedException("no"))
        .when(context)
        .validateImparticionBelongsToCentroAdmin(ADMIN_ID, 99);

    assertThatThrownBy(() -> service.obtenerParaEditar(ADMIN_ID, 99))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void crear_ok_siGrupoPertenece() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(grupoRepository.existsByIdAndCentroIdIn(eq(5), anyCollection())).thenReturn(true);
    AdminImparticionFormDTO form = new AdminImparticionFormDTO();
    form.setGrupoId(5);
    doNothing().when(adminImparticionService).crear(form);

    service.crear(ADMIN_ID, form);

    verify(adminImparticionService).crear(form);
  }

  @Test
  void crear_lanza_siGrupoNoPertenece() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(grupoRepository.existsByIdAndCentroIdIn(eq(99), anyCollection())).thenReturn(false);
    AdminImparticionFormDTO form = new AdminImparticionFormDTO();
    form.setGrupoId(99);

    assertThatThrownBy(() -> service.crear(ADMIN_ID, form))
        .isInstanceOf(AccessDeniedException.class);
    verify(adminImparticionService, never()).crear(any());
  }

  @Test
  void eliminar_ok_siPertenece() {
    doNothing().when(context).validateImparticionBelongsToCentroAdmin(ADMIN_ID, 7);
    doNothing().when(adminImparticionService).eliminar(7);

    service.eliminar(ADMIN_ID, 7);

    verify(adminImparticionService).eliminar(7);
  }

  @Test
  void eliminar_lanza_siEsDeOtroCentro() {
    doThrow(new AccessDeniedException("no"))
        .when(context)
        .validateImparticionBelongsToCentroAdmin(ADMIN_ID, 99);

    assertThatThrownBy(() -> service.eliminar(ADMIN_ID, 99))
        .isInstanceOf(AccessDeniedException.class);
    verify(adminImparticionService, never()).eliminar(anyInt());
  }
}
