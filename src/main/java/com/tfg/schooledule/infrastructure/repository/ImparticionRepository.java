package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Imparticion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImparticionRepository extends JpaRepository<Imparticion, Integer> {

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
}
