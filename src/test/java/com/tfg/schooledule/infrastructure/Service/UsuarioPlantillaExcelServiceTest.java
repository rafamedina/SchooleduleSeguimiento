package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class UsuarioPlantillaExcelServiceTest {

  private final UsuarioPlantillaExcelService service = new UsuarioPlantillaExcelService();

  @Test
  void generarPlantilla_retornaBytesNoVacios() {
    byte[] bytes = service.generarPlantilla();

    assertThat(bytes).isNotEmpty();
  }

  @Test
  void generarPlantilla_esArchivoExcelValido() throws Exception {
    byte[] bytes = service.generarPlantilla();

    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      assertThat(wb.getNumberOfSheets()).isPositive();
    }
  }

  @Test
  void generarPlantilla_primeraFilaConCabecerasCorrectas() throws Exception {
    byte[] bytes = service.generarPlantilla();

    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      var hoja = wb.getSheetAt(0);
      var cabecera = hoja.getRow(0);
      assertThat(cabecera.getCell(0).getStringCellValue()).isEqualTo("username");
      assertThat(cabecera.getCell(4).getStringCellValue()).isEqualTo("password");
      assertThat(cabecera.getCell(5).getStringCellValue()).isEqualTo("centro_nombre");
      assertThat(cabecera.getCell(7).getStringCellValue()).isEqualTo("grupo_nombre");
    }
  }

  @Test
  void generarPlantilla_tieneFilaDeEjemplo() throws Exception {
    byte[] bytes = service.generarPlantilla();

    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      var hoja = wb.getSheetAt(0);
      var filaEjemplo = hoja.getRow(1);
      String valorA2 = filaEjemplo.getCell(0).getStringCellValue();
      assertThat(valorA2).isNotEmpty();
    }
  }
}
