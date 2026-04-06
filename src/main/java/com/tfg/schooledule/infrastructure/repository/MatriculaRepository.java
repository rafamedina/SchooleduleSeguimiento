package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Matricula;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {
  List<Matricula> findByAlumnoId(Integer alumnoId);

  Optional<Matricula> findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(
      Integer alumnoId);
}
