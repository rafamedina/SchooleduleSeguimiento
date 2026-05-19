package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Modulo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

  List<Modulo> findAllByOrderByNombreAsc();

  @Query(
      "SELECT m FROM Modulo m WHERE :nombre IS NULL OR LOWER(m.nombre) LIKE LOWER(CONCAT('%',:nombre,'%')) ORDER BY m.nombre ASC")
  List<Modulo> findByNombreContaining(@Param("nombre") String nombre);

  Optional<Modulo> findByCodigo(String codigo);

  boolean existsByCodigo(String codigo);

  boolean existsByCodigoAndIdNot(String codigo, Integer id);

  List<Modulo> findByActivoTrueOrderByNombreAsc();
}
