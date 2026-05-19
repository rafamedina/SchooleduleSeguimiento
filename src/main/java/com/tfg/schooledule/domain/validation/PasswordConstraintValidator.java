package com.tfg.schooledule.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) return true;
    if (value.length() < 8) return false;
    if (value.chars().noneMatch(Character::isUpperCase)) return false;
    if (value.chars().noneMatch(Character::isLowerCase)) return false;
    return value.chars().anyMatch(Character::isDigit);
  }
}
