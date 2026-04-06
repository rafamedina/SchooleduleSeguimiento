package com.tfg.schooledule.domain.entity;

import jakarta.persistence.*;
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
}
