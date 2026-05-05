package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Modulo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

  List<Modulo> findAllByOrderByNombreAsc();

  boolean existsByCodigo(String codigo);

  boolean existsByCodigoAndIdNot(String codigo, Integer id);

  List<Modulo> findByActivoTrueOrderByNombreAsc();
}
