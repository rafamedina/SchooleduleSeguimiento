package com.tfg.schooledule.domain.entity;

import com.tfg.schooledule.domain.enums.EstadoMatricula;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "matriculas",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"alumno_id", "imparticion_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matricula {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "alumno_id", nullable = false)
  private Usuario alumno;

  @ManyToOne(optional = false)
  @JoinColumn(name = "imparticion_id", nullable = false)
  private Imparticion imparticion;

  @ManyToOne(optional = false)
  @JoinColumn(name = "centro_id", nullable = false)
  private Centro centro;

  @Column(name = "es_repetidor")
  @Builder.Default
  private Boolean esRepetidor = false;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "estado_matricula")
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
  private EstadoMatricula estado;
}
