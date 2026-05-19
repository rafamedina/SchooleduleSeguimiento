package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.AuditoriaNota;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaNotaRepository extends JpaRepository<AuditoriaNota, Integer> {

  List<AuditoriaNota> findByCalificacionId(Integer calificacionId);

  long countByCalificacionId(Integer calificacionId);

  @Query(
      """
      SELECT a FROM AuditoriaNota a
      JOIN FETCH a.calificacion c
      JOIN FETCH c.matricula m
      JOIN FETCH m.alumno
      JOIN FETCH m.imparticion i
      JOIN FETCH i.modulo
      JOIN FETCH i.centro
      ORDER BY a.fechaCambio DESC
      """)
  List<AuditoriaNota> findAllWithDetails();

  @Query(
      """
      SELECT a FROM AuditoriaNota a
      JOIN FETCH a.calificacion c
      JOIN FETCH c.matricula m
      JOIN FETCH m.alumno
      JOIN FETCH m.imparticion i
      JOIN FETCH i.modulo
      JOIN FETCH i.centro ct
      WHERE ct.id IN :centroIds
      ORDER BY a.fechaCambio DESC
      """)
  List<AuditoriaNota> findAllByCentroIds(@Param("centroIds") Set<Integer> centroIds);
}
