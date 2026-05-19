package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.CentroAdminStatsDTO;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroAdminDashboardService {

  private final CentroAdminContextService context;
  private final GrupoRepository grupoRepository;
  private final ImparticionRepository imparticionRepository;
  private final MatriculaRepository matriculaRepository;

  public CentroAdminDashboardService(
      CentroAdminContextService context,
      GrupoRepository grupoRepository,
      ImparticionRepository imparticionRepository,
      MatriculaRepository matriculaRepository) {
    this.context = context;
    this.grupoRepository = grupoRepository;
    this.imparticionRepository = imparticionRepository;
    this.matriculaRepository = matriculaRepository;
  }

  @Transactional(readOnly = true)
  public CentroAdminStatsDTO buildStats(Integer adminId) {
    var centros = context.getCentrosDelAdmin(adminId);
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);

    long numGrupos = centroIds.stream().mapToLong(grupoRepository::countByCentroId).sum();
    long numImparticiones = imparticionRepository.countByCentroIdIn(centroIds);
    long numAlumnos = matriculaRepository.countAlumnosActivosByCentroIds(centroIds);

    return new CentroAdminStatsDTO(centros.size(), numGrupos, numImparticiones, numAlumnos);
  }
}
