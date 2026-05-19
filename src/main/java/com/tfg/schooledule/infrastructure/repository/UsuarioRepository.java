package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
  Optional<Usuario> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmailAndIdNot(String email, Integer id);

  boolean existsByUsernameAndIdNot(String username, Integer id);

  List<Usuario> findAllByOrderByApellidosAscNombreAsc();

  @org.springframework.data.jpa.repository.Query(
      "SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_ALUMNO' ORDER BY u.apellidos ASC, u.nombre ASC")
  List<Usuario> findAllAlumnosOrdenados();

  @org.springframework.data.jpa.repository.Query(
      "SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_PROFESOR' ORDER BY u.apellidos ASC, u.nombre ASC")
  List<Usuario> findAllProfesoresOrdenados();

  Usuario findByEmail(String email);

  Optional<Usuario> findUsuarioByEmail(String correo);

  @org.springframework.data.jpa.repository.Query(
      "SELECT COUNT(u) FROM Usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_ADMIN' AND u.activo = true")
  long countAdminsActivos();

  @org.springframework.data.jpa.repository.Query(
      "SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_ADMIN_CENTRO' ORDER BY u.apellidos ASC, u.nombre ASC")
  List<Usuario> findAllAdminCentroOrdenados();

  @Query(
      """
      SELECT DISTINCT u FROM Usuario u LEFT JOIN u.roles r
      WHERE (:rolNombre IS NULL OR r.nombre = :rolNombre)
      ORDER BY u.apellidos ASC, u.nombre ASC
      """)
  List<Usuario> findByRol(@Param("rolNombre") String rolNombre);

  @Query(
      """
      SELECT DISTINCT u FROM Usuario u
      JOIN u.roles r, Matricula m
      WHERE m.alumno = u
        AND r.nombre = 'ROLE_ALUMNO'
        AND (:centroId IS NULL OR m.centro.id = :centroId)
        AND (:grupoId  IS NULL OR m.imparticion.grupo.id = :grupoId)
        AND (:cursoId  IS NULL OR m.imparticion.grupo.cursoAcademico.id = :cursoId)
      ORDER BY u.apellidos ASC, u.nombre ASC
      """)
  List<Usuario> findAlumnosByFiltro(
      @Param("centroId") Integer centroId,
      @Param("grupoId") Integer grupoId,
      @Param("cursoId") Integer cursoId);
}
