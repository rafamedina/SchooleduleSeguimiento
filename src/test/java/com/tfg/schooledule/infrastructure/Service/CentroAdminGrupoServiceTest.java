package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminGrupoFormDTO;
import com.tfg.schooledule.domain.dto.AdminGrupoListDTO;
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
class CentroAdminGrupoServiceTest {

  @Mock private CentroAdminContextService context;
  @Mock private AdminGrupoService adminGrupoService;
  @Mock private GrupoRepository grupoRepository;
  @Mock private ImparticionRepository imparticionRepository;

  @InjectMocks private CentroAdminGrupoService service;

  private static final int ADMIN_ID = 10;
  private static final int CENTRO_A = 1;

  private Centro centroA;
  private CursoAcademico curso;
  private Grupo grupo;

  @BeforeEach
  void setUp() {
    centroA = new Centro();
    centroA.setId(CENTRO_A);
    centroA.setNombre("IES Getafe");

    curso = new CursoAcademico();
    curso.setId(1);
    curso.setNombre("2025/2026");

    grupo = new Grupo();
    grupo.setId(5);
    grupo.setNombre("1DAW-A");
    grupo.setCentro(centroA);
    grupo.setCursoAcademico(curso);
  }

  @Test
  void listarGrupos_retornaGruposDelAdmin() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(grupoRepository.findByCentroIdInOrderByCentroNombreAscNombreAsc(anyCollection()))
        .thenReturn(List.of(grupo));
    when(imparticionRepository.countByGrupoId(5)).thenReturn(2);

    List<AdminGrupoListDTO> result = service.listarGruposDeCentros(ADMIN_ID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).nombre()).isEqualTo("1DAW-A");
  }

  @Test
  void obtenerParaEditar_ok_siGrupoPertenece() {
    doNothing().when(context).validateGrupoBelongsToCentroAdmin(ADMIN_ID, 5);
    var dto = new AdminGrupoFormDTO();
    when(adminGrupoService.obtenerParaEditar(5)).thenReturn(dto);

    AdminGrupoFormDTO result = service.obtenerParaEditar(ADMIN_ID, 5);

    assertThat(result).isSameAs(dto);
  }

  @Test
  void obtenerParaEditar_lanza_siGrupoEsDeOtroCentro() {
    doThrow(new AccessDeniedException("no"))
        .when(context)
        .validateGrupoBelongsToCentroAdmin(ADMIN_ID, 99);

    assertThatThrownBy(() -> service.obtenerParaEditar(ADMIN_ID, 99))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void crear_ok_siCentroPertenece() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    AdminGrupoFormDTO form = new AdminGrupoFormDTO();
    form.setCentroId(CENTRO_A);
    doNothing().when(adminGrupoService).crear(form);

    service.crear(ADMIN_ID, form);

    verify(adminGrupoService).crear(form);
  }

  @Test
  void crear_lanza_siCentroNoPertenece() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    AdminGrupoFormDTO form = new AdminGrupoFormDTO();
    form.setCentroId(99);

    assertThatThrownBy(() -> service.crear(ADMIN_ID, form))
        .isInstanceOf(AccessDeniedException.class);
    verify(adminGrupoService, never()).crear(any());
  }

  @Test
  void eliminar_ok_siGrupoPertenece() {
    doNothing().when(context).validateGrupoBelongsToCentroAdmin(ADMIN_ID, 5);
    doNothing().when(adminGrupoService).eliminar(5);

    service.eliminar(ADMIN_ID, 5);

    verify(adminGrupoService).eliminar(5);
  }

  @Test
  void eliminar_lanza_siGrupoEsDeOtroCentro() {
    doThrow(new AccessDeniedException("no"))
        .when(context)
        .validateGrupoBelongsToCentroAdmin(ADMIN_ID, 99);

    assertThatThrownBy(() -> service.eliminar(ADMIN_ID, 99))
        .isInstanceOf(AccessDeniedException.class);
    verify(adminGrupoService, never()).eliminar(anyInt());
  }
}
