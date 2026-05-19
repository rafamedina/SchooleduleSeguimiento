package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tfg.schooledule.domain.dto.UsuarioImportRowDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class UsuarioExcelParserServiceTest {

  private final UsuarioExcelParserService parser = new UsuarioExcelParserService();

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

  private byte[] crearExcel(List<String[]> filasDatos) throws IOException {
    XSSFWorkbook wb = new XSSFWorkbook();
    Sheet hoja = wb.createSheet();
    Row cabecera = hoja.createRow(0);
    for (int i = 0; i < CABECERAS.length; i++) cabecera.createCell(i).setCellValue(CABECERAS[i]);
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

  private String[] filaValida(String username) {
    return new String[] {
      username,
      "Juan",
      "García",
      "juan@ies.es",
      "Abc12345",
      "IES La Madraza",
      "2025/2026",
      "DAW 1A",
      "no"
    };
  }

  @Test
  void parsear_archivoValido_devuelveLasFilasCorrectas() throws Exception {
    byte[] bytes = crearExcel(filas(filaValida("alumno01"), filaValida("alumno02")));

    List<UsuarioImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado).hasSize(2);
    assertThat(resultado.get(0).username()).isEqualTo("alumno01");
    assertThat(resultado.get(0).nombre()).isEqualTo("Juan");
    assertThat(resultado.get(0).apellidos()).isEqualTo("García");
    assertThat(resultado.get(0).email()).isEqualTo("juan@ies.es");
    assertThat(resultado.get(0).password()).isEqualTo("Abc12345");
    assertThat(resultado.get(0).centroNombre()).isEqualTo("IES La Madraza");
    assertThat(resultado.get(0).cursoAcademicoNombre()).isEqualTo("2025/2026");
    assertThat(resultado.get(0).grupoNombre()).isEqualTo("DAW 1A");
    assertThat(resultado.get(1).username()).isEqualTo("alumno02");
  }

  @Test
  void parsear_emailVacio_devuelveNull() throws Exception {
    byte[] bytes =
        crearExcel(
            filas(
                new String[] {
                  "alumno01",
                  "Juan",
                  "García",
                  null,
                  "Abc12345",
                  "Centro",
                  "2025/2026",
                  "Grupo",
                  null
                }));

    List<UsuarioImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado.get(0).email()).isNull();
  }

  @Test
  void parsear_esRepetidorVacio_devuelveNull() throws Exception {
    byte[] bytes =
        crearExcel(
            filas(
                new String[] {
                  "alumno01",
                  "Juan",
                  "García",
                  "j@e.es",
                  "Abc12345",
                  "Centro",
                  "2025/2026",
                  "Grupo",
                  null
                }));

    List<UsuarioImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado.get(0).esRepetidorRaw()).isNull();
  }

  @Test
  void parsear_filaCompletamenteVacia_laIgnora() throws Exception {
    byte[] bytes =
        crearExcel(
            filas(
                filaValida("alumno01"),
                new String[] {null, null, null, null, null, null, null, null, null},
                filaValida("alumno02")));

    List<UsuarioImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado).hasSize(2);
  }

  @Test
  void parsear_soloFilaDeCabecera_devuelveListaVacia() throws Exception {
    byte[] bytes = crearExcel(new ArrayList<>());

    List<UsuarioImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado).isEmpty();
  }

  @Test
  void parsear_archivoBinarioInvalido_lanzaIllegalArgumentException() {
    byte[] bytes = new byte[] {0x00, 0x01, 0x02, 0x03};

    assertThatThrownBy(() -> parser.parsear(bytes))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No se pudo leer");
  }

  @Test
  void parsear_numeroFilaEsCorrecto() throws Exception {
    byte[] bytes = crearExcel(filas(filaValida("alumno01"), filaValida("alumno02")));

    List<UsuarioImportRowDTO> resultado = parser.parsear(bytes);

    assertThat(resultado.get(0).numeroFila()).isEqualTo(1);
    assertThat(resultado.get(1).numeroFila()).isEqualTo(2);
  }
}
