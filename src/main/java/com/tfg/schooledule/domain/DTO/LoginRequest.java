package com.tfg.schooledule.domain.DTO;

import lombok.Data;

@Data
public class LoginRequest {
  public String email;
  public String password;
}
