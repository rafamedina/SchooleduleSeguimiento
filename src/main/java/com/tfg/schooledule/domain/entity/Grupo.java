package com.tfg.schooledule.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grupos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grupo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 50)
  private String nombre;

  @ManyToOne(optional = false)
  @JoinColumn(name = "centro_id", nullable = false)
  private Centro centro;

  @ManyToOne(optional = false)
  @JoinColumn(name = "curso_academico_id", nullable = false)
  private CursoAcademico cursoAcademico;
}
