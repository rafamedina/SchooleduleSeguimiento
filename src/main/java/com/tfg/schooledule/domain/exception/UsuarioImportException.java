package com.tfg.schooledule.domain.exception;

import com.tfg.schooledule.domain.dto.UsuarioImportErrorDTO;
import java.util.List;

public class UsuarioImportException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final transient List<UsuarioImportErrorDTO> errores;

  public UsuarioImportException(List<UsuarioImportErrorDTO> errores) {
    super("Importación de usuarios fallida con " + errores.size() + " error(es)");
    this.errores = List.copyOf(errores);
  }

  public List<UsuarioImportErrorDTO> getErrores() {
    return errores;
  }
}
