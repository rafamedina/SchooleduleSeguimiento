package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminAuditoriaListDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaExcelExportService {

  private static final String[] HEADERS = {
    "#",
    "Alumno (Email)",
    "Módulo",
    "Centro",
    "Valor Anterior",
    "Valor Nuevo",
    "Responsable",
    "Fecha Cambio",
    "Motivo"
  };

  private static final DateTimeFormatter FECHA_FMT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  public byte[] exportar(List<AdminAuditoriaListDTO> registros) throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Auditoría");

      CellStyle headerStyle = buildHeaderStyle(workbook);

      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < HEADERS.length; i++) {
        var cell = headerRow.createCell(i);
        cell.setCellValue(HEADERS[i]);
        cell.setCellStyle(headerStyle);
      }

      int rowNum = 1;
      for (AdminAuditoriaListDTO r : registros) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(r.id());
        row.createCell(1).setCellValue(r.alumnoEmail());
        row.createCell(2).setCellValue(r.moduloNombre());
        row.createCell(3).setCellValue(r.centroNombre());
        row.createCell(4)
            .setCellValue(r.valorAnterior() != null ? r.valorAnterior().toPlainString() : "—");
        row.createCell(5)
            .setCellValue(r.valorNuevo() != null ? r.valorNuevo().toPlainString() : "—");
        row.createCell(6).setCellValue(r.usuarioResponsable());
        row.createCell(7)
            .setCellValue(r.fechaCambio() != null ? r.fechaCambio().format(FECHA_FMT) : "");
        row.createCell(8).setCellValue(r.motivo() != null ? r.motivo() : "—");
      }

      for (int i = 0; i < HEADERS.length; i++) {
        sheet.autoSizeColumn(i);
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      workbook.write(out);
      return out.toByteArray();
    }
  }

  private CellStyle buildHeaderStyle(XSSFWorkbook workbook) {
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());

    CellStyle style = workbook.createCellStyle();
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return style;
  }
}
