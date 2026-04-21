package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.CriterioEvaluacion;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriterioEvaluacionRepository extends JpaRepository<CriterioEvaluacion, Integer> {

  List<CriterioEvaluacion> findByResultadoAprendizajeIdOrderByCodigoAsc(Integer raId);

  List<CriterioEvaluacion> findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(
      Collection<Integer> raIds);
}
