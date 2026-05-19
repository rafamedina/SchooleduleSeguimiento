package com.tfg.schooledule.domain.entity;

import com.tfg.schooledule.domain.enums.InstrumentoEvaluacion;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "criterios_evaluacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterioEvaluacion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "resultado_aprendizaje_id", nullable = false)
  private ResultadoAprendizaje resultadoAprendizaje;

  @Column(nullable = false, length = 20)
  private String codigo;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String descripcion;

  @Column(nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal peso = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private InstrumentoEvaluacion instrumento;

  @Column(name = "unidad_didactica", length = 20)
  private String unidadDidactica;

  @Column private Short trimestre;
}
