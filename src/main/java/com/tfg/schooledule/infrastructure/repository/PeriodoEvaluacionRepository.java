package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.PeriodoEvaluacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodoEvaluacionRepository extends JpaRepository<PeriodoEvaluacion, Integer> {
  List<PeriodoEvaluacion> findByImparticionId(Integer imparticionId);
}
