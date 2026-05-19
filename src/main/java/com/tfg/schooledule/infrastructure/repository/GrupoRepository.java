package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Grupo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

  @Query(
      """
      SELECT g FROM Grupo g
      WHERE (:centroId IS NULL OR g.centro.id = :centroId)
        AND (:cursoId   IS NULL OR g.cursoAcademico.id = :cursoId)
      ORDER BY g.centro.nombre ASC, g.nombre ASC
      """)
  List<Grupo> findByFiltro(@Param("centroId") Integer centroId, @Param("cursoId") Integer cursoId);

  boolean existsByCentroId(Integer centroId);

  long countByCentroId(Integer centroId);

  List<Grupo> findAllByOrderByCentroNombreAscNombreAsc();

  int countByCursoAcademicoId(Integer cursoId);

  boolean existsByNombreAndCentroIdAndCursoAcademicoId(
      String nombre, Integer centroId, Integer cursoId);

  boolean existsByNombreAndCentroIdAndCursoAcademicoIdAndIdNot(
      String nombre, Integer centroId, Integer cursoId, Integer id);

  List<Grupo> findByTutorId(Integer tutorId);

  boolean existsByIdAndTutorId(Integer grupoId, Integer tutorId);

  List<Grupo> findByCentroIdInOrderByCentroNombreAscNombreAsc(
      java.util.Collection<Integer> centroIds);

  boolean existsByIdAndCentroIdIn(Integer grupoId, java.util.Collection<Integer> centroIds);

  java.util.Optional<Grupo> findByNombreIgnoreCaseAndCentroIdAndCursoAcademicoId(
      String nombre, Integer centroId, Integer cursoAcademicoId);
}
