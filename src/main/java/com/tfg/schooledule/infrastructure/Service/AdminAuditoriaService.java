package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminAuditoriaListDTO;
import com.tfg.schooledule.domain.entity.AuditoriaNota;
import com.tfg.schooledule.infrastructure.repository.AuditoriaNotaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditoriaService {

  private final AuditoriaNotaRepository auditoriaNotaRepository;

  public AdminAuditoriaService(AuditoriaNotaRepository auditoriaNotaRepository) {
    this.auditoriaNotaRepository = auditoriaNotaRepository;
  }

  @Transactional(readOnly = true)
  public List<AdminAuditoriaListDTO> buscar(
      String alumnoEmail, String moduloNombre, LocalDate fechaDesde, LocalDate fechaHasta) {

    String emailFilter = (alumnoEmail != null) ? alumnoEmail.trim().toLowerCase() : "";
    String moduloFilter = (moduloNombre != null) ? moduloNombre.trim().toLowerCase() : "";
    LocalDateTime dtDesde = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
    LocalDateTime dtHasta = fechaHasta != null ? fechaHasta.atTime(23, 59, 59) : null;

    Stream<AuditoriaNota> stream = auditoriaNotaRepository.findAllWithDetails().stream();

    if (!emailFilter.isEmpty()) {
      stream =
          stream.filter(
              a ->
                  a.getCalificacion()
                      .getMatricula()
                      .getAlumno()
                      .getEmail()
                      .toLowerCase()
                      .contains(emailFilter));
    }
    if (!moduloFilter.isEmpty()) {
      stream =
          stream.filter(
              a ->
                  a.getCalificacion()
                      .getMatricula()
                      .getImparticion()
                      .getModulo()
                      .getNombre()
                      .toLowerCase()
                      .contains(moduloFilter));
    }
    if (dtDesde != null) {
      stream = stream.filter(a -> !a.getFechaCambio().isBefore(dtDesde));
    }
    if (dtHasta != null) {
      stream = stream.filter(a -> !a.getFechaCambio().isAfter(dtHasta));
    }

    return stream.map(this::toDTO).toList();
  }

  private AdminAuditoriaListDTO toDTO(AuditoriaNota a) {
    return new AdminAuditoriaListDTO(
        a.getId(),
        a.getCalificacion().getMatricula().getAlumno().getEmail(),
        a.getCalificacion().getMatricula().getImparticion().getModulo().getNombre(),
        a.getValorAnterior(),
        a.getValorNuevo(),
        a.getUsuarioResponsable(),
        a.getFechaCambio(),
        a.getMotivo());
  }
}
