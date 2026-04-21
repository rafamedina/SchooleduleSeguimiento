package com.tfg.schooledule.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.tfg.schooledule.domain.dto.GradeDTO;
import com.tfg.schooledule.domain.entity.Calificacion;
import com.tfg.schooledule.domain.entity.CriterioEvaluacion;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class GradeMapperTest {

  private final GradeMapper mapper = Mappers.getMapper(GradeMapper.class);

  @Test
  void toDto_mapsCriterioEvaluacionFields() {
    CriterioEvaluacion ce =
        CriterioEvaluacion.builder().id(1).codigo("a").descripcion("Examen Final").build();
    Calificacion calif =
        Calificacion.builder()
            .criterioEvaluacion(ce)
            .valor(new BigDecimal("9.0"))
            .comentario("Muy bien")
            .build();

    GradeDTO dto = mapper.toDto(calif);

    assertEquals("a – Examen Final", dto.itemNombre());
    assertEquals(new BigDecimal("9.0"), dto.valor());
    assertEquals("Muy bien", dto.comentario());
    assertNull(dto.fecha());
    assertNull(dto.tipoActividad());
  }

  @Test
  void toDtoList_convertsAllElements() {
    CriterioEvaluacion ce =
        CriterioEvaluacion.builder().id(1).codigo("a").descripcion("T1").build();
    List<Calificacion> list =
        List.of(
            Calificacion.builder().criterioEvaluacion(ce).valor(BigDecimal.valueOf(7)).build(),
            Calificacion.builder().criterioEvaluacion(ce).valor(BigDecimal.valueOf(8)).build());

    List<GradeDTO> dtos = mapper.toDtoList(list);

    assertEquals(2, dtos.size());
  }
}
