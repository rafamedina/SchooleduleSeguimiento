package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.ResultadoAprendizaje;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultadoAprendizajeRepository
    extends JpaRepository<ResultadoAprendizaje, Integer> {

  List<ResultadoAprendizaje> findByModuloIdOrderByCodigoAsc(Integer moduloId);

  List<ResultadoAprendizaje> findByModuloIdOrderByCursoAcademicoIdAscCodigoAsc(Integer moduloId);

  boolean existsByModuloId(Integer moduloId);

  boolean existsByModuloIdAndCursoAcademicoId(Integer moduloId, Integer cursoAcademicoId);

  int countByModuloId(Integer moduloId);

  @Query(
      "SELECT DISTINCT ra.cursoAcademico.nombre FROM ResultadoAprendizaje ra"
          + " WHERE ra.modulo.id = :moduloId ORDER BY ra.cursoAcademico.nombre")
  List<String> findNombresCursosConRasByModuloId(@Param("moduloId") Integer moduloId);
}
