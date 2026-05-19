package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.ChangePasswordForm;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/change-password")
@PreAuthorize("isAuthenticated()")
public class ChangePasswordController {

  private static final String VIEW_CHANGE_PASSWORD = "change-password";

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  public ChangePasswordController(
      UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping
  public String showForm(Principal principal, Model model) {
    Usuario usuario = resolveUsuario(principal);
    if (!Boolean.TRUE.equals(usuario.getMustChangePassword())) {
      return redirectToDashboard(usuario);
    }
    model.addAttribute("form", new ChangePasswordForm());
    return VIEW_CHANGE_PASSWORD;
  }

  @PostMapping
  public String handleSubmit(
      @Valid @ModelAttribute("form") ChangePasswordForm form,
      BindingResult result,
      Principal principal,
      Model model,
      RedirectAttributes redirectAttributes) {

    Usuario usuario = resolveUsuario(principal);

    if (!Boolean.TRUE.equals(usuario.getMustChangePassword())) {
      return redirectToDashboard(usuario);
    }

    if (result.hasErrors()) {
      return VIEW_CHANGE_PASSWORD;
    }

    if (!form.getNuevaPassword().equals(form.getConfirmarPassword())) {
      model.addAttribute("errorConfirm", "Las contraseñas no coinciden.");
      return VIEW_CHANGE_PASSWORD;
    }

    usuario.setPasswordHash(passwordEncoder.encode(form.getNuevaPassword()));
    usuario.setMustChangePassword(false);
    usuarioRepository.save(usuario);

    redirectAttributes.addFlashAttribute("successMessage", "Contraseña actualizada correctamente.");
    return redirectToDashboard(usuario);
  }

  private Usuario resolveUsuario(Principal principal) {
    return usuarioRepository
        .findUsuarioByEmail(principal.getName())
        .orElseThrow(
            () -> new jakarta.persistence.EntityNotFoundException("Usuario no encontrado"));
  }

  private String redirectToDashboard(Usuario usuario) {
    boolean isAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre().contains("ADMIN"));
    boolean isProfesor =
        usuario.getRoles().stream().anyMatch(r -> r.getNombre().contains("PROFESOR"));
    if (isAdmin) return "redirect:/admin/dashboard";
    if (isProfesor) return "redirect:/profe/dashboard";
    return "redirect:/alumno/dashboard";
  }
}
