package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Imparticion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImparticionRepository extends JpaRepository<Imparticion, Integer> {

  List<Imparticion> findByProfesorIdAndCentroId(Integer profesorId, Integer centroId);

  boolean existsByIdAndProfesorId(Integer id, Integer profesorId);
}
