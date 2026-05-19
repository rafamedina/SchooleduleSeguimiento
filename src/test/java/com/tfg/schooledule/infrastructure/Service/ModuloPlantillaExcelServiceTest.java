package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ModuloPlantillaExcelServiceTest {

  private final ModuloPlantillaExcelService service = new ModuloPlantillaExcelService();

  @Test
  void generarPlantilla_retornaBytesNoVacios() {
    byte[] bytes = service.generarPlantilla();
    assertThat(bytes).isNotEmpty();
  }

  @Test
  void generarPlantilla_esArchivoExcelValido() {
    byte[] bytes = service.generarPlantilla();
    assertThatCode(() -> new XSSFWorkbook(new ByteArrayInputStream(bytes)).close())
        .doesNotThrowAnyException();
  }

  @Test
  void generarPlantilla_primeraFilaConCabecerasCorrectas() throws Exception {
    byte[] bytes = service.generarPlantilla();
    DataFormatter fmt = new DataFormatter();
    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      var hoja = wb.getSheetAt(0);
      var cabecera = hoja.getRow(0);
      assertThat(fmt.formatCellValue(cabecera.getCell(0))).isEqualTo("ra_codigo");
      assertThat(fmt.formatCellValue(cabecera.getCell(3))).isEqualTo("ce_codigo");
      assertThat(fmt.formatCellValue(cabecera.getCell(6))).isEqualTo("instrumento");
    }
  }

  @Test
  void generarPlantilla_tieneFilaDeEjemplo() throws Exception {
    byte[] bytes = service.generarPlantilla();
    DataFormatter fmt = new DataFormatter();
    try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      var hoja = wb.getSheetAt(0);
      var ejemplo = hoja.getRow(1);
      assertThat((Object) ejemplo).isNotNull();
      assertThat(fmt.formatCellValue(ejemplo.getCell(0))).isNotBlank();
    }
  }
}
