package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.AuditoriaNota;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaNotaRepository extends JpaRepository<AuditoriaNota, Integer> {

  List<AuditoriaNota> findByCalificacionId(Integer calificacionId);
}
