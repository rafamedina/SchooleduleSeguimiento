package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.CursoAcademico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CursoAcademicoRepository extends JpaRepository<CursoAcademico, Integer> {

  List<CursoAcademico> findAllByOrderByNombreAsc();

  List<CursoAcademico> findAllByOrderByFechaInicioDesc();

  List<CursoAcademico> findByActivoTrueOrderByNombreAsc();

  Optional<CursoAcademico> findByActivo(boolean activo);

  boolean existsByNombre(String nombre);

  boolean existsByNombreAndIdNot(String nombre, Integer id);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE CursoAcademico c SET c.activo = false WHERE c.id <> :id")
  int desactivarTodosExcepto(@Param("id") Integer id);
}
