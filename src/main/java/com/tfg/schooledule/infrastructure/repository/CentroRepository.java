package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Centro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroRepository extends JpaRepository<Centro, Integer> {

  List<Centro> findAllByOrderByNombreAsc();

  @Query(
      "SELECT c FROM Centro c WHERE :nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%',:nombre,'%')) ORDER BY c.nombre ASC")
  List<Centro> findByNombreContaining(@Param("nombre") String nombre);

  boolean existsByNombre(String nombre);

  boolean existsByNombreAndIdNot(String nombre, Integer id);

  List<Centro> findAllByActivoTrueOrderByNombreAsc();

  java.util.Optional<Centro> findByNombreIgnoreCase(String nombre);
}
