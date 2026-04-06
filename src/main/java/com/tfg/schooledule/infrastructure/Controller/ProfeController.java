package com.tfg.schooledule.infrastructure.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profe")
public class ProfeController {

  @GetMapping("/dashboard")
  public String panelProfesor() {
    return "profe/menuProfesor";
  }
}
