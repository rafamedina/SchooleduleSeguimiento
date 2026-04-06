package com.tfg.schooledule.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "periodos_evaluacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoEvaluacion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "imparticion_id", nullable = false)
  private Imparticion imparticion;

  @Column(nullable = false, length = 50)
  private String nombre;

  @Column(precision = 5, scale = 2)
  private BigDecimal peso;

  @Column(nullable = false)
  @Builder.Default
  private Boolean cerrado = false;
}
