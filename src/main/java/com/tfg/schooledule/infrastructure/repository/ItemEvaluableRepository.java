package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.ItemEvaluable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemEvaluableRepository extends JpaRepository<ItemEvaluable, Integer> {

  List<ItemEvaluable> findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(
      Integer imparticionId);
}
