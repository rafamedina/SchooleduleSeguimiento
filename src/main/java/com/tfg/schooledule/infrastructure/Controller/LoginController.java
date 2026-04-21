package com.tfg.schooledule.infrastructure.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping("/login")
  public String vistaLogin() {
    return "login";
  }

  @GetMapping("/seleccionar-rol")
  public String vistaSeleccionarRol(Authentication authentication, Model model) {
    java.util.Set<String> roles =
        org.springframework.security.core.authority.AuthorityUtils.authorityListToSet(
            authentication.getAuthorities());
    model.addAttribute("roles", roles);
    return "seleccionar-rol";
  }
}
