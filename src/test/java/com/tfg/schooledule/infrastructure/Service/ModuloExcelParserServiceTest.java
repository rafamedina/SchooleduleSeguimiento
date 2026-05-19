package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.*;

import com.tfg.schooledule.domain.dto.ModuloImportRowDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ModuloExcelParserServiceTest {

  private final ModuloExcelParserService parser = new ModuloExcelParserService();

  private byte[] crearExcel(List<String[]> filasDatos) throws IOException {
    XSSFWorkbook wb = new XSSFWorkbook();
    Sheet hoja = wb.createSheet();
    Row cabecera = hoja.createRow(0);
    String[] cols = {
      "ra_codigo", "ra_descripcion", "ra_peso",
      "ce_codigo", "ce_descripcion", "ce_peso",
      "instrumento", "unidad_didactica", "trimestre"
    };
    for (int i = 0; i < cols.length; i++) cabecera.createCell(i).setCellValue(cols[i]);
    for (int r = 0; r < filasDatos.size(); r++) {
      Row fila = hoja.createRow(r + 1);
      String[] vals = filasDatos.get(r);
      for (int c = 0; c < vals.length; c++) {
        if (vals[c] != null) fila.createCell(c).setCellValue(vals[c]);
      }
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    wb.write(bos);
    wb.close();
    return bos.toByteArray();
  }

  private List<String[]> filas(String[]... arrays) {
    List<String[]> lista = new ArrayList<>();
    for (String[] arr : arrays) lista.add(arr);
    return lista;
  }

  @Test
  void parsear_archivoValido_devuelveLasFilasCorrectas() throws Exception {
    byte[] bytes =
        crearExcel(
            filas(
                new String[] {
                  "RA1", "Descripción RA1", "15", "1a", "Desc CE 1a", "20", null, null, null
                },
                new String[] {
                  "RA1", "Descripción RA1", "15", "1b", "Desc CE 1b", "30", null, null, null
                },
                new String[] {
                  "RA2", "Descripción RA2", "25", "2a", "Desc CE 2a", "50", null, null, null
                }));

    List<ModuloImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado).hasSize(3);
    assertThat(resultado.get(0).raCodigo()).isEqualTo("RA1");
    assertThat(resultado.get(0).ceCodigo()).isEqualTo("1a");
    assertThat(resultado.get(2).raCodigo()).isEqualTo("RA2");
  }

  @Test
  void parsear_camposNumericos_convierteCorrectamente() throws Exception {
    byte[] bytes =
        crearExcel(
            filas(new String[] {"RA1", "Desc", "15.5", "1a", "Desc CE", "20.0", null, null, null}));

    List<ModuloImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).raPeso()).isEqualByComparingTo(new BigDecimal("15.5"));
    assertThat(resultado.get(0).cePeso()).isEqualByComparingTo(new BigDecimal("20.0"));
  }

  @Test
  void parsear_filaCompletamenteVacia_laIgnora() throws Exception {
    byte[] bytes =
        crearExcel(
            filas(
                new String[] {"RA1", "Desc RA1", "15", "1a", "Desc CE 1a", "20", null, null, null},
                new String[] {null, null, null, null, null, null, null, null, null},
                new String[] {
                  "RA2", "Desc RA2", "25", "2a", "Desc CE 2a", "50", null, null, null
                }));

    List<ModuloImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado).hasSize(2);
  }

  @Test
  void parsear_celdaOpcionalVacia_devuelveNull() throws Exception {
    byte[] bytes =
        crearExcel(
            filas(new String[] {"RA1", "Desc", "15", "1a", "Desc CE", "20", null, null, null}));

    List<ModuloImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado.get(0).instrumento()).isNull();
    assertThat(resultado.get(0).unidadDidactica()).isNull();
    assertThat(resultado.get(0).trimestre()).isNull();
  }

  @Test
  void parsear_archivoBinarioInvalido_lanzaIllegalArgumentException() {
    byte[] bytes = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04};

    assertThatThrownBy(() -> parser.parsear(bytes))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No se pudo leer");
  }

  @Test
  void parsear_soloFilaDeCabecera_devuelveListaVacia() throws Exception {
    byte[] bytes = crearExcel(new ArrayList<>());

    List<ModuloImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado).isEmpty();
  }

  @Test
  void parsear_camposOpcionalesRellenos_losLeeCorrectamente() throws Exception {
    byte[] bytes =
        crearExcel(
            filas(
                new String[] {
                  "RA1", "Desc", "15", "1a", "Desc CE", "20", "PRUEBA_OBJETIVA", "UD1", "1"
                }));

    List<ModuloImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado.get(0).instrumento()).isEqualTo("PRUEBA_OBJETIVA");
    assertThat(resultado.get(0).unidadDidactica()).isEqualTo("UD1");
    assertThat(resultado.get(0).trimestre()).isEqualTo(1);
  }
}
