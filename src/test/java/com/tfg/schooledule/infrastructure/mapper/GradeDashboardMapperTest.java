package com.tfg.schooledule.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.tfg.schooledule.domain.dto.GradeDashboardDTO;
import com.tfg.schooledule.domain.entity.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class GradeDashboardMapperTest {

  private GradeDashboardMapper mapper;

  @BeforeEach
  void setUp() throws Exception {
    GradeMapper gradeMapper = Mappers.getMapper(GradeMapper.class);
    mapper = new GradeDashboardMapperImpl();
    GradeDashboardMapperImpl impl = (GradeDashboardMapperImpl) mapper;
    impl.gradeMapper = gradeMapper;
  }

  @Test
  void toDto_groupsCalificacionesByModulo() {
    Modulo modulo1 = Modulo.builder().nombre("Programacion").build();
    Modulo modulo2 = Modulo.builder().nombre("Bases de Datos").build();
    CriterioEvaluacion ce =
        CriterioEvaluacion.builder().id(1).codigo("a").descripcion("E1").build();

    Imparticion imp1 = Imparticion.builder().modulo(modulo1).build();
    Imparticion imp2 = Imparticion.builder().modulo(modulo2).build();
    Matricula m1 = Matricula.builder().imparticion(imp1).build();
    Matricula m2 = Matricula.builder().imparticion(imp2).build();

    List<Calificacion> califs =
        List.of(
            Calificacion.builder()
                .matricula(m1)
                .criterioEvaluacion(ce)
                .valor(BigDecimal.valueOf(8))
                .build(),
            Calificacion.builder()
                .matricula(m1)
                .criterioEvaluacion(ce)
                .valor(BigDecimal.valueOf(9))
                .build(),
            Calificacion.builder()
                .matricula(m2)
                .criterioEvaluacion(ce)
                .valor(BigDecimal.valueOf(7))
                .build());

    GradeDashboardDTO dto = mapper.toDto(califs, "1er Trimestre");

    assertEquals("1er Trimestre", dto.periodoNombre());
    assertEquals(2, dto.gradesByModulo().size());
    assertEquals(2, dto.gradesByModulo().get("Programacion").size());
    assertEquals(1, dto.gradesByModulo().get("Bases de Datos").size());
  }

  @Test
  void toDto_emptyListReturnsEmptyMap() {
    GradeDashboardDTO dto = mapper.toDto(Collections.emptyList(), null);

    assertNotNull(dto);
    assertTrue(dto.gradesByModulo().isEmpty());
    assertNull(dto.periodoNombre());
  }
}
