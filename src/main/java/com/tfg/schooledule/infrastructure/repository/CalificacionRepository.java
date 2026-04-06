package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Calificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Integer> {

  @Query(
      "SELECT c FROM Calificacion c "
          + "JOIN c.matricula m "
          + "JOIN c.itemEvaluable i "
          + "WHERE m.alumno.id = :alumnoId "
          + "AND i.periodoEvaluacion.id = :periodoId")
  List<Calificacion> findByAlumnoIdAndPeriodoId(
      @Param("alumnoId") Integer alumnoId, @Param("periodoId") Integer periodoId);

  @Query("SELECT c FROM Calificacion c " + "JOIN c.matricula m " + "WHERE m.alumno.id = :alumnoId")
  List<Calificacion> findByAlumnoId(@Param("alumnoId") Integer alumnoId);
}
