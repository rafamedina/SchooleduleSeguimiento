package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.ResultadoAprendizaje;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultadoAprendizajeRepository
    extends JpaRepository<ResultadoAprendizaje, Integer> {

  List<ResultadoAprendizaje> findByModuloIdOrderByCodigoAsc(Integer moduloId);
}
