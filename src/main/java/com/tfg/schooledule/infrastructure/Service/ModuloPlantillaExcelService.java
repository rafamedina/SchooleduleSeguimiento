package com.tfg.schooledule.infrastructure.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

@Service
public class ModuloPlantillaExcelService {

  private static final String[] CABECERAS = {
    "ra_codigo", "ra_descripcion", "ra_peso",
    "ce_codigo", "ce_descripcion", "ce_peso",
    "instrumento", "unidad_didactica", "trimestre"
  };

  private static final String[] EJEMPLO = {
    "RA1", "Comprende los fundamentos de ASP.NET", "30",
    "1a", "Se han identificado los componentes de ASP.NET", "25",
    "PRUEBA_OBJETIVA", "UD1", "1"
  };

  public byte[] generarPlantilla() {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      XSSFSheet hoja = wb.createSheet("Importar RAs");

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

      XSSFComment comentario =
          hoja.createDrawingPatriarch()
              .createCellComment(new XSSFClientAnchor(0, 0, 0, 0, 6, 1, 10, 5));
      comentario.setString(
          new XSSFRichTextString(
              """
              Valores válidos:
              PRUEBA_OBJETIVA
              ACTIVIDAD_EVALUABLE
              TRABAJO_PROYECTO
              EXPOSICION_ORAL
              OBSERVACION_ACTITUD"""));
      filaEjemplo.getCell(6).setCellComment(comentario);

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
