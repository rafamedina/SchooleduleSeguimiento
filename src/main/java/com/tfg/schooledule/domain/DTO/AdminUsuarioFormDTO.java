package com.tfg.schooledule.domain.dto;

import com.tfg.schooledule.domain.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Formulario de creación/edición de un usuario del sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsuarioFormDTO {

  @Schema(description = "ID del usuario (null en creación, presente en edición)", example = "3")
  private Integer id;

  @Schema(
      description =
          "Nombre de usuario único. Solo letras, números, puntos, guiones y guiones bajos. Entre 3 y 50 caracteres.",
      example = "ana.garcia")
  @NotBlank
  @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$")
  private String username;

  @Schema(description = "Nombre del usuario (max 100 caracteres)", example = "Ana")
  @NotBlank
  @Size(max = 100)
  private String nombre;

  @Schema(description = "Apellidos del usuario (max 100 caracteres)", example = "García López")
  @NotBlank
  @Size(max = 100)
  private String apellidos;

  @Schema(description = "Email único del usuario. Se usa como login.", example = "ana@ies.es")
  @NotBlank
  @Email
  @Size(max = 254)
  private String email;

  @Schema(
      description =
          "Contraseña (@ValidPassword: mín. 8 chars, al menos 1 mayúscula, 1 número y 1 carácter especial). "
              + "En edición: si está vacío no se modifica la contraseña actual.")
  @ValidPassword
  private String password;

  @Schema(
      description =
          "IDs de los roles asignados al usuario (ej: [1] = ADMIN, [2] = PROFESOR, [3] = ALUMNO)")
  private Set<Integer> roleIds = new HashSet<>();

  @Schema(description = "IDs de los centros educativos a los que pertenece el usuario")
  private Set<Integer> centroIds = new HashSet<>();
}
