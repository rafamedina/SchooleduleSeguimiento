package com.tfg.schooledule.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "calificaciones",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"matricula_id", "criterio_evaluacion_id"})
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calificacion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "matricula_id", nullable = false)
  private Matricula matricula;

  @ManyToOne(optional = false)
  @JoinColumn(name = "criterio_evaluacion_id", nullable = false)
  private CriterioEvaluacion criterioEvaluacion;

  @Column(precision = 5, scale = 2)
  private BigDecimal valor;

  @Column(columnDefinition = "TEXT")
  private String comentario;

  @UpdateTimestamp
  @Column(name = "fecha_modificacion")
  private LocalDateTime fechaModificacion;
}
