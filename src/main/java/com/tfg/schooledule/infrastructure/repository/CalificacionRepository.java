package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Calificacion;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Integer> {

  @Query(
      "SELECT c FROM Calificacion c "
          + "JOIN c.matricula m "
          + "JOIN c.criterioEvaluacion ce "
          + "JOIN ce.resultadoAprendizaje ra "
          + "WHERE m.alumno.id = :alumnoId "
          + "AND EXISTS ("
          + "  SELECT ie FROM ItemEvaluable ie "
          + "  WHERE ie.resultadoAprendizaje = ra "
          + "  AND ie.imparticion = m.imparticion "
          + "  AND ie.periodoEvaluacion.id = :periodoId"
          + ")")
  List<Calificacion> findByAlumnoIdAndPeriodoId(
      @Param("alumnoId") Integer alumnoId, @Param("periodoId") Integer periodoId);

  @Query("SELECT c FROM Calificacion c JOIN c.matricula m WHERE m.alumno.id = :alumnoId")
  List<Calificacion> findByAlumnoId(@Param("alumnoId") Integer alumnoId);

  Optional<Calificacion> findByMatriculaIdAndCriterioEvaluacionId(
      Integer matriculaId, Integer criterioEvaluacionId);

  List<Calificacion> findByMatriculaIdAndCriterioEvaluacionIdIn(
      Integer matriculaId, Collection<Integer> criterioIds);

  List<Calificacion> findByMatriculaId(Integer matriculaId);
}
