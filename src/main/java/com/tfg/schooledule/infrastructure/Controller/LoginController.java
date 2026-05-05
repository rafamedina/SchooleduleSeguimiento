package com.tfg.schooledule.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Autenticación")
@Controller
public class LoginController {

  @Operation(
      summary = "Formulario de login",
      description =
          "Muestra el formulario de autenticación (email + contraseña). "
              + "El POST de credenciales lo procesa Spring Security en /login. "
              + "Con ?error muestra mensaje de error. Con ?logout muestra mensaje de cierre de sesión.")
  @ApiResponse(responseCode = "200", description = "Vista HTML: login")
  @SecurityRequirements
  @GetMapping("/login")
  public String vistaLogin() {
    return "login";
  }

  @Operation(
      summary = "Selección de rol",
      description =
          "Vista que se muestra cuando el usuario autenticado tiene más de un rol asignado. "
              + "Permite elegir con qué rol continuar la sesión. "
              + "Solo accesible tras autenticación exitosa.")
  @ApiResponse(
      responseCode = "200",
      description =
          "Vista HTML: seleccionar-rol. Modelo: roles (Set<String> con los roles del usuario)")
  @ApiResponse(responseCode = "302", description = "Redirect a /login si no está autenticado")
  @GetMapping("/seleccionar-rol")
  public String vistaSeleccionarRol(Authentication authentication, Model model) {
    java.util.Set<String> roles =
        org.springframework.security.core.authority.AuthorityUtils.authorityListToSet(
            authentication.getAuthorities());
    model.addAttribute("roles", roles);
    return "seleccionar-rol";
  }
}
