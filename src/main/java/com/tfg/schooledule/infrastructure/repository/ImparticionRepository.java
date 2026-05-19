package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Imparticion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImparticionRepository extends JpaRepository<Imparticion, Integer> {

  @Query(
      """
      SELECT i FROM Imparticion i
      WHERE (:centroId   IS NULL OR i.centro.id = :centroId)
        AND (:grupoId    IS NULL OR i.grupo.id  = :grupoId)
        AND (:moduloId   IS NULL OR i.modulo.id = :moduloId)
        AND (:profesorId IS NULL OR i.profesor.id = :profesorId)
        AND (:cursoId    IS NULL OR i.grupo.cursoAcademico.id = :cursoId)
      ORDER BY i.grupo.nombre ASC, i.modulo.nombre ASC
      """)
  List<Imparticion> findByFiltro(
      @Param("centroId") Integer centroId,
      @Param("grupoId") Integer grupoId,
      @Param("moduloId") Integer moduloId,
      @Param("profesorId") Integer profesorId,
      @Param("cursoId") Integer cursoId);

  List<Imparticion> findByProfesorIdAndCentroId(Integer profesorId, Integer centroId);

  boolean existsByIdAndProfesorId(Integer id, Integer profesorId);

  boolean existsByProfesorIdAndCentroId(Integer profesorId, Integer centroId);

  boolean existsByModuloId(Integer moduloId);

  int countByModuloId(Integer moduloId);

  int countByGrupoId(Integer grupoId);

  boolean existsByGrupoId(Integer grupoId);

  java.util.List<Imparticion> findAllByOrderByGrupoNombreAscModuloNombreAsc();

  boolean existsByModuloIdAndGrupoId(Integer moduloId, Integer grupoId);

  boolean existsByModuloIdAndGrupoIdAndIdNot(Integer moduloId, Integer grupoId, Integer id);

  List<Imparticion> findByGrupoId(Integer grupoId);

  List<Imparticion> findByCentroIdInOrderByGrupoNombreAscModuloNombreAsc(
      java.util.Collection<Integer> centroIds);

  boolean existsByIdAndCentroIdIn(Integer id, java.util.Collection<Integer> centroIds);

  long countByCentroIdIn(java.util.Collection<Integer> centroIds);
}
