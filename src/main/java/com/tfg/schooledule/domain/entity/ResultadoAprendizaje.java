package com.tfg.schooledule.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resultados_aprendizaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoAprendizaje {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "modulo_id", nullable = false)
  private Modulo modulo;

  @ManyToOne(optional = false)
  @JoinColumn(name = "curso_academico_id", nullable = false)
  private CursoAcademico cursoAcademico;

  @Column(nullable = false, length = 20)
  private String codigo;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String descripcion;

  @Column(name = "peso_sugerido", precision = 5, scale = 2)
  private BigDecimal pesoSugerido;
}
