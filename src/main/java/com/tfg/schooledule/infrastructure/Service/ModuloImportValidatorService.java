package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.ModuloImportErrorDTO;
import com.tfg.schooledule.domain.dto.ModuloImportPreviewDTO;
import com.tfg.schooledule.domain.dto.ModuloImportRowDTO;
import com.tfg.schooledule.domain.enums.InstrumentoEvaluacion;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ModuloImportValidatorService {

  private static final int MAX_RAS = 15;
  private static final int MAX_CES = 100;
  private static final BigDecimal CERO = BigDecimal.ZERO;
  private static final BigDecimal CIEN = new BigDecimal("100");
  private static final String CAMPO_ARCHIVO = "archivo";

  public ModuloImportPreviewDTO validar(List<ModuloImportRowDTO> filas) {
    List<ModuloImportErrorDTO> errores = new ArrayList<>();

    if (filas.isEmpty()) {
      errores.add(
          new ModuloImportErrorDTO(0, CAMPO_ARCHIVO, "El archivo no contiene filas de datos"));
      return new ModuloImportPreviewDTO(false, errores, List.of(), 0, 0);
    }

    validarLimites(filas, errores);
    filas.forEach(fila -> validarCamposRequeridos(fila, errores));
    validarConsistenciaRas(filas, errores);
    validarDuplicadosCes(filas, errores);

    List<String> advertencias = errores.isEmpty() ? calcularAdvertencias(filas) : List.of();
    long totalRas =
        filas.stream()
            .map(ModuloImportRowDTO::raCodigo)
            .filter(Objects::nonNull)
            .filter(c -> !c.isBlank())
            .distinct()
            .count();

    return new ModuloImportPreviewDTO(
        errores.isEmpty(), errores, advertencias, (int) totalRas, filas.size());
  }

  private void validarLimites(List<ModuloImportRowDTO> filas, List<ModuloImportErrorDTO> errores) {
    long distinctRas =
        filas.stream()
            .map(ModuloImportRowDTO::raCodigo)
            .filter(Objects::nonNull)
            .filter(c -> !c.isBlank())
            .distinct()
            .count();
    if (distinctRas > MAX_RAS) {
      errores.add(
          new ModuloImportErrorDTO(
              0, CAMPO_ARCHIVO, "El archivo supera el límite de 15 RAs por módulo"));
    }
    if (filas.size() > MAX_CES) {
      errores.add(
          new ModuloImportErrorDTO(0, CAMPO_ARCHIVO, "El archivo supera el límite de 100 CEs"));
    }
  }

  private void validarCamposRequeridos(
      ModuloImportRowDTO fila, List<ModuloImportErrorDTO> errores) {
    int n = fila.numeroFila();

    if (isBlank(fila.raCodigo())) {
      errores.add(new ModuloImportErrorDTO(n, "ra_codigo", "El código de RA es obligatorio"));
    }
    if (isBlank(fila.raDescripcion())) {
      errores.add(
          new ModuloImportErrorDTO(n, "ra_descripcion", "La descripción de RA es obligatoria"));
    }
    if (fila.raPeso() == null
        || fila.raPeso().compareTo(CERO) <= 0
        || fila.raPeso().compareTo(CIEN) > 0) {
      errores.add(
          new ModuloImportErrorDTO(n, "ra_peso", "El peso de RA debe ser un número entre 1 y 100"));
    }
    if (isBlank(fila.ceCodigo())) {
      errores.add(new ModuloImportErrorDTO(n, "ce_codigo", "El código de CE es obligatorio"));
    }
    if (isBlank(fila.ceDescripcion())) {
      errores.add(
          new ModuloImportErrorDTO(n, "ce_descripcion", "La descripción de CE es obligatoria"));
    }
    if (fila.cePeso() == null
        || fila.cePeso().compareTo(CERO) <= 0
        || fila.cePeso().compareTo(CIEN) > 0) {
      errores.add(
          new ModuloImportErrorDTO(n, "ce_peso", "El peso de CE debe ser un número entre 1 y 100"));
    }
    if (fila.instrumento() != null
        && !fila.instrumento().isBlank()
        && InstrumentoEvaluacion.fromTexto(fila.instrumento()).isEmpty()) {
      errores.add(
          new ModuloImportErrorDTO(
              n,
              "instrumento",
              "Instrumento no válido. Valores aceptados: PRUEBA_OBJETIVA,"
                  + " ACTIVIDAD_EVALUABLE, TRABAJO_PROYECTO, EXPOSICION_ORAL,"
                  + " OBSERVACION_ACTITUD"));
    }
    if (fila.trimestre() != null && fila.trimestre() != 1 && fila.trimestre() != 2) {
      errores.add(new ModuloImportErrorDTO(n, "trimestre", "El trimestre debe ser 1 o 2"));
    }
  }

  private void validarConsistenciaRas(
      List<ModuloImportRowDTO> filas, List<ModuloImportErrorDTO> errores) {
    Map<String, ModuloImportRowDTO> primeraFila = new LinkedHashMap<>();
    for (ModuloImportRowDTO fila : filas) {
      if (isBlank(fila.raCodigo())) continue;
      String cod = fila.raCodigo().trim();
      ModuloImportRowDTO ref = primeraFila.get(cod);
      if (ref == null) {
        primeraFila.put(cod, fila);
      } else {
        if (!Objects.equals(trimOrNull(fila.raDescripcion()), trimOrNull(ref.raDescripcion()))) {
          errores.add(
              new ModuloImportErrorDTO(
                  fila.numeroFila(),
                  "ra_descripcion",
                  "La descripción del RA '" + cod + "' no es consistente entre filas"));
        }
        if (fila.raPeso() != null
            && ref.raPeso() != null
            && fila.raPeso().compareTo(ref.raPeso()) != 0) {
          errores.add(
              new ModuloImportErrorDTO(
                  fila.numeroFila(),
                  "ra_peso",
                  "El peso del RA '" + cod + "' no es consistente entre filas"));
        }
      }
    }
  }

  private void validarDuplicadosCes(
      List<ModuloImportRowDTO> filas, List<ModuloImportErrorDTO> errores) {
    Map<String, Set<String>> cesPorRa = new LinkedHashMap<>();
    for (ModuloImportRowDTO fila : filas) {
      if (isBlank(fila.raCodigo()) || isBlank(fila.ceCodigo())) continue;
      String raCod = fila.raCodigo().trim();
      String ceCod = fila.ceCodigo().trim();
      Set<String> ces = cesPorRa.computeIfAbsent(raCod, k -> new LinkedHashSet<>());
      if (!ces.add(ceCod)) {
        errores.add(
            new ModuloImportErrorDTO(
                fila.numeroFila(),
                "ce_codigo",
                "CE duplicado '" + ceCod + "' en RA '" + raCod + "'"));
      }
    }
  }

  private List<String> calcularAdvertencias(List<ModuloImportRowDTO> filas) {
    List<String> advertencias = new ArrayList<>();

    Map<String, BigDecimal> pesosPorRa =
        filas.stream()
            .filter(f -> !isBlank(f.raCodigo()) && f.raPeso() != null)
            .collect(
                Collectors.toMap(
                    f -> f.raCodigo().trim(), ModuloImportRowDTO::raPeso, (a, b) -> a));

    BigDecimal sumaRa = pesosPorRa.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    if (sumaRa.compareTo(CIEN) != 0) {
      advertencias.add(
          "La suma de pesos de RAs es "
              + sumaRa.stripTrailingZeros().toPlainString()
              + "%, debería ser 100%");
    }

    Map<String, List<ModuloImportRowDTO>> filasPorRa =
        filas.stream()
            .filter(f -> !isBlank(f.raCodigo()))
            .collect(Collectors.groupingBy(f -> f.raCodigo().trim()));

    filasPorRa.forEach(
        (cod, grupo) -> {
          BigDecimal sumaCe =
              grupo.stream()
                  .filter(f -> f.cePeso() != null)
                  .map(ModuloImportRowDTO::cePeso)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          if (sumaCe.compareTo(CIEN) != 0) {
            advertencias.add(
                "La suma de pesos de CEs del RA '"
                    + cod
                    + "' es "
                    + sumaCe.stripTrailingZeros().toPlainString()
                    + "%, debería ser 100%");
          }
        });

    return advertencias;
  }

  private boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private String trimOrNull(String s) {
    return s == null ? null : s.trim();
  }
}
