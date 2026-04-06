package com.tfg.schooledule.infrastructure.security;

import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

  private final UsuarioRepository userRepository;

  public CustomUserDetailsService(UsuarioRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
    // Buscar primero por email, luego por username
    Usuario usuario =
        userRepository
            .findUsuarioByEmail(input)
            .or(() -> userRepository.findByUsername(input))
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + input));

    // Los roles en BD ya incluyen el prefijo ROLE_ (ej: ROLE_ADMIN, ROLE_PROFESOR,
    // ROLE_ALUMNO)
    var authorities =
        usuario.getRoles().stream()
            .map(
                rol -> {
                  String nombre = rol.getNombre();
                  if (!nombre.startsWith("ROLE_")) {
                    nombre = "ROLE_" + nombre;
                  }
                  return new SimpleGrantedAuthority(nombre);
                })
            .collect(Collectors.toSet());

    log.debug(
        "Cargando usuario: input={}, email={}, activo={}, roles={}",
        input,
        usuario.getEmail(),
        usuario.getActivo(),
        authorities);

    return new User(
        usuario.getEmail(),
        usuario.getPasswordHash(),
        usuario.getActivo(),
        true,
        true,
        true,
        authorities);
  }
}
