package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Matricula;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {
  List<Matricula> findByAlumnoId(Integer alumnoId);

  @Query(
      """
      SELECT m FROM Matricula m
        JOIN FETCH m.imparticion i
        JOIN FETCH i.modulo
        JOIN FETCH i.grupo g
        JOIN FETCH g.cursoAcademico
        JOIN FETCH i.centro
      WHERE m.alumno.id = :alumnoId
        AND m.estado = 'ACTIVA'
      """)
  List<Matricula> findActivasByAlumnoId(@Param("alumnoId") Integer alumnoId);

  Optional<Matricula> findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(
      Integer alumnoId);

  List<Matricula> findByImparticionIdAndEstado(Integer imparticionId, EstadoMatricula estado);

  Optional<Matricula> findByIdAndImparticionProfesorId(Integer id, Integer profesorId);
}
