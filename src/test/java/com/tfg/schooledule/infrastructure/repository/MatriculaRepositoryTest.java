package com.tfg.schooledule.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatriculaRepositoryTest {

  @PersistenceContext private EntityManager em;
  @Autowired private MatriculaRepository matriculaRepository;

  private <T> T save(T entity) {
    em.persist(entity);
    return entity;
  }

  private CursoAcademico buildCurso() {
    return save(
        CursoAcademico.builder()
            .nombre("2025-26")
            .fechaInicio(LocalDate.of(2025, 9, 1))
            .fechaFin(LocalDate.of(2026, 6, 30))
            .activo(true)
            .build());
  }

  @Test
  @Disabled("H2 NAMED_ENUM — ver conductor/tech-stack.md")
  void findActivasByAlumnoId_devuelveSoloMatriculasActivasConRelacionesCargadas() {
    CursoAcademico curso = buildCurso();
    Centro centro = save(Centro.builder().nombre("Centro Test").ubicacion("Madrid").build());

    Usuario profe =
        save(
            Usuario.builder()
                .username("profe")
                .passwordHash("h")
                .nombre("Profe")
                .apellidos("Test")
                .email("profe@t.com")
                .build());
    Usuario alumno =
        save(
            Usuario.builder()
                .username("alumno")
                .passwordHash("h")
                .nombre("Alumno")
                .apellidos("Test")
                .email("alumno@t.com")
                .build());

    Modulo mod1 = save(Modulo.builder().codigo("MOD1").nombre("Modulo 1").build());
    Modulo mod2 = save(Modulo.builder().codigo("MOD2").nombre("Modulo 2").build());
    Modulo mod3 = save(Modulo.builder().codigo("MOD3").nombre("Modulo 3").build());

    Grupo g = save(Grupo.builder().nombre("G1").centro(centro).cursoAcademico(curso).build());

    Imparticion imp1 =
        save(Imparticion.builder().modulo(mod1).grupo(g).profesor(profe).centro(centro).build());
    Imparticion imp2 =
        save(Imparticion.builder().modulo(mod2).grupo(g).profesor(profe).centro(centro).build());
    Imparticion imp3 =
        save(Imparticion.builder().modulo(mod3).grupo(g).profesor(profe).centro(centro).build());

    // 2 ACTIVA, 1 BAJA
    save(
        Matricula.builder()
            .alumno(alumno)
            .imparticion(imp1)
            .centro(centro)
            .estado(EstadoMatricula.ACTIVA)
            .build());
    save(
        Matricula.builder()
            .alumno(alumno)
            .imparticion(imp2)
            .centro(centro)
            .estado(EstadoMatricula.ACTIVA)
            .build());
    save(
        Matricula.builder()
            .alumno(alumno)
            .imparticion(imp3)
            .centro(centro)
            .estado(EstadoMatricula.BAJA)
            .build());

    em.flush();
    em.clear();

    List<Matricula> result = matriculaRepository.findActivasByAlumnoId(alumno.getId());

    assertThat(result).hasSize(2);
    assertThat(result).allMatch(m -> m.getEstado() == EstadoMatricula.ACTIVA);
    assertThat(result)
        .allMatch(
            m ->
                m.getImparticion() != null
                    && m.getImparticion().getModulo() != null
                    && m.getImparticion().getGrupo() != null
                    && m.getImparticion().getGrupo().getCursoAcademico() != null
                    && m.getImparticion().getCentro() != null);
  }
}
