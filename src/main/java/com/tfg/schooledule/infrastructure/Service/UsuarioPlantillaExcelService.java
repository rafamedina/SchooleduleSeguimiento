package com.tfg.schooledule.infrastructure.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class UsuarioPlantillaExcelService {

  private static final String[] CABECERAS = {
    "username",
    "nombre",
    "apellidos",
    "email",
    "password",
    "centro_nombre",
    "curso_academico_nombre",
    "grupo_nombre",
    "es_repetidor"
  };

  private static final String[] EJEMPLO = {
    "jperez2025",
    "Juan",
    "Pérez García",
    "juan.perez@ies.es",
    "Abc12345",
    "IES La Madraza",
    "2025/2026",
    "DAW 1A",
    "no"
  };

  public byte[] generarPlantilla() {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      XSSFSheet hoja = wb.createSheet("Importar Usuarios");

      CellStyle estiloCabecera = crearEstiloCabecera(wb);
      CellStyle estiloEjemplo = crearEstiloEjemplo(wb);

      Row filaCabecera = hoja.createRow(0);
      for (int i = 0; i < CABECERAS.length; i++) {
        Cell celda = filaCabecera.createCell(i);
        celda.setCellValue(CABECERAS[i]);
        celda.setCellStyle(estiloCabecera);
      }

      Row filaEjemplo = hoja.createRow(1);
      for (int i = 0; i < EJEMPLO.length; i++) {
        Cell celda = filaEjemplo.createCell(i);
        celda.setCellValue(EJEMPLO[i]);
        celda.setCellStyle(estiloEjemplo);
      }

      for (int i = 0; i < CABECERAS.length; i++) {
        hoja.autoSizeColumn(i);
      }
      hoja.createFreezePane(0, 1);

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      wb.write(bos);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo generar la plantilla Excel", e);
    }
  }

  private CellStyle crearEstiloCabecera(XSSFWorkbook wb) {
    XSSFCellStyle estilo = wb.createCellStyle();
    XSSFFont fuente = wb.createFont();
    fuente.setBold(true);
    fuente.setColor(new XSSFColor(new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, null));
    estilo.setFont(fuente);
    estilo.setFillForegroundColor(
        new XSSFColor(new byte[] {(byte) 0x2E, (byte) 0x3A, (byte) 0x5C}, null));
    estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return estilo;
  }

  private CellStyle crearEstiloEjemplo(XSSFWorkbook wb) {
    XSSFCellStyle estilo = wb.createCellStyle();
    estilo.setFillForegroundColor(
        new XSSFColor(new byte[] {(byte) 0xCC, (byte) 0xCC, (byte) 0xCC}, null));
    estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return estilo;
  }
}
