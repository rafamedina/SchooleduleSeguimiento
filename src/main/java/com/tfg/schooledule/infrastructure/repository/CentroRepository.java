package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Centro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroRepository extends JpaRepository<Centro, Integer> {

  List<Centro> findAllByOrderByNombreAsc();

  boolean existsByNombre(String nombre);

  boolean existsByNombreAndIdNot(String nombre, Integer id);

  List<Centro> findAllByActivoTrueOrderByNombreAsc();
}
