package com.tfg.schooledule.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ImparticionRepositoryTest {

  @PersistenceContext private EntityManager em;
  @Autowired private ImparticionRepository imparticionRepository;
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

  /** Verifica que findByProfesorIdAndCentroId filtra correctamente por profesor Y centro. */
  @Test
  void findByProfesorIdAndCentroId_aislaPorProfesorYCentro() {
    CursoAcademico curso = buildCurso();
    Centro centro1 = save(Centro.builder().nombre("C1").ubicacion("Madrid").build());
    Centro centro2 = save(Centro.builder().nombre("C2").ubicacion("Barcelona").build());

    Usuario profe1 =
        save(
            Usuario.builder()
                .username("p1test")
                .passwordHash("h")
                .nombre("Juan")
                .apellidos("G")
                .email("p1test@t.com")
                .build());
    Usuario profe2 =
        save(
            Usuario.builder()
                .username("p2test")
                .passwordHash("h")
                .nombre("Ana")
                .apellidos("G")
                .email("p2test@t.com")
                .build());

    Modulo mod = save(Modulo.builder().codigo("M1T").nombre("Módulo1").build());
    Grupo g1 = save(Grupo.builder().nombre("G1").centro(centro1).cursoAcademico(curso).build());
    Grupo g2 = save(Grupo.builder().nombre("G2").centro(centro2).cursoAcademico(curso).build());

    // 3 imparticiones: profe1@centro1, profe1@centro2, profe2@centro1
    Imparticion imp1 =
        save(Imparticion.builder().modulo(mod).grupo(g1).profesor(profe1).centro(centro1).build());
    save(Imparticion.builder().modulo(mod).grupo(g2).profesor(profe1).centro(centro2).build());
    save(Imparticion.builder().modulo(mod).grupo(g1).profesor(profe2).centro(centro1).build());
    em.flush();
    em.clear();

    List<Imparticion> result =
        imparticionRepository.findByProfesorIdAndCentroId(profe1.getId(), centro1.getId());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCentro().getId()).isEqualTo(centro1.getId());

    // profe2 no puede ver la imparticion de profe1
    assertThat(imparticionRepository.existsByIdAndProfesorId(imp1.getId(), profe2.getId()))
        .isFalse();
    assertThat(imparticionRepository.existsByIdAndProfesorId(imp1.getId(), profe1.getId()))
        .isTrue();
  }

  /**
   * El test de MatriculaRepository.findByImparticionIdAndEstado se omite aquí porque H2 no admite
   * el tipo NAMED_ENUM de Hibernate. Esta lógica queda cubierta por TeacherDashboardServiceTest
   * (Mockito) y ProfeControllerIntegrationTest (Testcontainers).
   *
   * @deprecated marcado para referencia; el test real está en la suite de integración.
   */
  @Test
  @org.junit.jupiter.api.Disabled(
      "H2 NAMED_ENUM incompatible con PostgreSQL — cubierto en service tests")
  void findByImparticionIdAndEstado_filtraPorEstado() {
    CursoAcademico curso = buildCurso();
    Centro centro = save(Centro.builder().nombre("Ct").ubicacion("M").build());

    Usuario profe =
        save(
            Usuario.builder()
                .username("pftest")
                .passwordHash("h")
                .nombre("Pf")
                .apellidos("A")
                .email("pftest@t.com")
                .build());
    Usuario alumno1 =
        save(
            Usuario.builder()
                .username("al1test")
                .passwordHash("h")
                .nombre("Al1")
                .apellidos("B")
                .email("al1test@t.com")
                .build());
    Usuario alumno2 =
        save(
            Usuario.builder()
                .username("al2test")
                .passwordHash("h")
                .nombre("Al2")
                .apellidos("C")
                .email("al2test@t.com")
                .build());

    Modulo mod = save(Modulo.builder().codigo("M2T").nombre("M2").build());
    Grupo g = save(Grupo.builder().nombre("Gt").centro(centro).cursoAcademico(curso).build());
    Imparticion imp =
        save(Imparticion.builder().modulo(mod).grupo(g).profesor(profe).centro(centro).build());

    save(
        Matricula.builder()
            .alumno(alumno1)
            .imparticion(imp)
            .centro(centro)
            .estado(EstadoMatricula.ACTIVA)
            .build());
    save(
        Matricula.builder()
            .alumno(alumno2)
            .imparticion(imp)
            .centro(centro)
            .estado(EstadoMatricula.BAJA)
            .build());
    em.flush();
    em.clear();

    List<Matricula> activas =
        matriculaRepository.findByImparticionIdAndEstado(imp.getId(), EstadoMatricula.ACTIVA);
    assertThat(activas).hasSize(1);
    assertThat(activas.get(0).getEstado()).isEqualTo(EstadoMatricula.ACTIVA);

    // findByIdAndImparticionProfesorId: el alumno pertenece a la imparticion de ese profe
    Optional<Matricula> found =
        matriculaRepository.findByIdAndImparticionProfesorId(activas.get(0).getId(), profe.getId());
    assertThat(found).isPresent();

    // profe incorrecto → vacío
    Optional<Matricula> notFound =
        matriculaRepository.findByIdAndImparticionProfesorId(activas.get(0).getId(), 9999);
    assertThat(notFound).isEmpty();
  }
}
