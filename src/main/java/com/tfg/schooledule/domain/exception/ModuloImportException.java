package com.tfg.schooledule.domain.exception;

import com.tfg.schooledule.domain.dto.ModuloImportErrorDTO;
import java.util.List;

public class ModuloImportException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final transient List<ModuloImportErrorDTO> errores;

  public ModuloImportException(List<ModuloImportErrorDTO> errores) {
    super("Importación fallida con " + errores.size() + " error(es)");
    this.errores = List.copyOf(errores);
  }

  public List<ModuloImportErrorDTO> getErrores() {
    return errores;
  }
}
