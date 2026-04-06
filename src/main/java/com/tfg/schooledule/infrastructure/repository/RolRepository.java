package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
  Rol findByNombre(String nombre);
}
