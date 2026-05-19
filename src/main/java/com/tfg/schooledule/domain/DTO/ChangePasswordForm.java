package com.tfg.schooledule.domain.dto;

import com.tfg.schooledule.domain.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public class ChangePasswordForm {

  @ValidPassword @NotBlank private String nuevaPassword;

  @NotBlank private String confirmarPassword;

  public String getNuevaPassword() {
    return nuevaPassword;
  }

  public void setNuevaPassword(String nuevaPassword) {
    this.nuevaPassword = nuevaPassword;
  }

  public String getConfirmarPassword() {
    return confirmarPassword;
  }

  public void setConfirmarPassword(String confirmarPassword) {
    this.confirmarPassword = confirmarPassword;
  }
}
