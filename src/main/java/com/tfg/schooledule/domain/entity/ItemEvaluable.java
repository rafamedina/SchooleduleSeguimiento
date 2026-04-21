package com.tfg.schooledule.domain.entity;

import com.tfg.schooledule.domain.enums.TipoActividad;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "items_evaluables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEvaluable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "imparticion_id", nullable = false)
  private Imparticion imparticion;

  @ManyToOne(optional = false)
  @JoinColumn(name = "periodo_evaluacion_id", nullable = false)
  private PeriodoEvaluacion periodoEvaluacion;

  @Column(nullable = false, length = 100)
  private String nombre;

  private LocalDate fecha;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "tipo_actividad", nullable = false)
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
  private TipoActividad tipo;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "resultado_aprendizaje_id", nullable = false)
  private ResultadoAprendizaje resultadoAprendizaje;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "configuracion_rubrica", columnDefinition = "jsonb")
  private Map<String, Object> configuracionRubrica;
}
