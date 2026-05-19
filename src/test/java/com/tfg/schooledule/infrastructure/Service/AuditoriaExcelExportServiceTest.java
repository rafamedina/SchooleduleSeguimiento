package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tfg.schooledule.domain.dto.AdminAuditoriaListDTO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class AuditoriaExcelExportServiceTest {

  private final AuditoriaExcelExportService service = new AuditoriaExcelExportService();

  private AdminAuditoriaListDTO buildDTO() {
    return new AdminAuditoriaListDTO(
        1,
        "alumno@test.com",
        "Programación",
        10,
        "IES Getafe",
        new BigDecimal("5.00"),
        new BigDecimal("8.00"),
        "prof@test.com",
        LocalDateTime.of(2025, 3, 15, 10, 30),
        "Corrección");
  }

  @Test
  void exportar_listaVacia_generaExcelConCabeceraYSinFilas() throws IOException {
    byte[] bytes = service.exportar(Collections.emptyList());

    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = wb.getSheet("Auditoría");
      assertThat(sheet).isNotNull();
      assertThat(sheet.getLastRowNum()).isZero();
      assertThat((int) sheet.getRow(0).getLastCellNum()).isEqualTo(9);
    }
  }

  @Test
  void exportar_conRegistros_generaFilasCorrectamente() throws IOException {
    byte[] bytes = service.exportar(List.of(buildDTO()));

    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = wb.getSheet("Auditoría");
      assertThat(sheet.getLastRowNum()).isEqualTo(1);
      var row = sheet.getRow(1);
      assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(1.0);
      assertThat(row.getCell(1).getStringCellValue()).isEqualTo("alumno@test.com");
      assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Programación");
      assertThat(row.getCell(3).getStringCellValue()).isEqualTo("IES Getafe");
      assertThat(row.getCell(4).getStringCellValue()).isEqualTo("5.00");
      assertThat(row.getCell(5).getStringCellValue()).isEqualTo("8.00");
      assertThat(row.getCell(6).getStringCellValue()).isEqualTo("prof@test.com");
      assertThat(row.getCell(8).getStringCellValue()).isEqualTo("Corrección");
    }
  }

  @Test
  void exportar_formatoFecha_usaPatternddMMyyyyHHmm() throws IOException {
    byte[] bytes = service.exportar(List.of(buildDTO()));

    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = wb.getSheet("Auditoría");
      String fecha = sheet.getRow(1).getCell(7).getStringCellValue();
      assertThat(fecha).isEqualTo("15/03/2025 10:30");
    }
  }
}
