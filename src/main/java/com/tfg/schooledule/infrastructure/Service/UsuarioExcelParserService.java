package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.UsuarioImportRowDTO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class UsuarioExcelParserService {

  public List<UsuarioImportRowDTO> parsear(byte[] bytes) {
    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet hoja = wb.getSheetAt(0);
      List<UsuarioImportRowDTO> resultado = new ArrayList<>();
      DataFormatter fmt = new DataFormatter();
      for (int i = 1; i <= hoja.getLastRowNum(); i++) {
        Row fila = hoja.getRow(i);
        if (esFilaVacia(fila, fmt)) continue;
        resultado.add(mapearFila(i, fila, fmt));
      }
      return resultado;
    } catch (IOException | RuntimeException e) {
      throw new IllegalArgumentException("No se pudo leer el archivo: " + e.getMessage(), e);
    }
  }

  private boolean esFilaVacia(Row fila, DataFormatter fmt) {
    if (fila == null) return true;
    for (int c = 0; c < 9; c++) {
      String val = fmt.formatCellValue(fila.getCell(c));
      if (val != null && !val.isBlank()) return false;
    }
    return true;
  }

  private UsuarioImportRowDTO mapearFila(int idx, Row fila, DataFormatter fmt) {
    return new UsuarioImportRowDTO(
        idx,
        leerTexto(fila, 0, fmt),
        leerTexto(fila, 1, fmt),
        leerTexto(fila, 2, fmt),
        leerTexto(fila, 3, fmt),
        leerTexto(fila, 4, fmt),
        leerTexto(fila, 5, fmt),
        leerTexto(fila, 6, fmt),
        leerTexto(fila, 7, fmt),
        leerTexto(fila, 8, fmt));
  }

  private String leerTexto(Row fila, int col, DataFormatter fmt) {
    String val = fmt.formatCellValue(fila.getCell(col)).trim();
    return val.isBlank() ? null : val;
  }
}
