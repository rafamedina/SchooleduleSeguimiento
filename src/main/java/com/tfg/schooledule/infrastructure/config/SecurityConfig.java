package com.tfg.schooledule.infrastructure.config;

import com.tfg.schooledule.infrastructure.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  @Autowired private CustomLoginSuccessHandler successHandler;
  private final CustomUserDetailsService userDetailsService;

  public SecurityConfig(CustomUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  // @Bean
  // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
  // Exception {
  // http
  // .authorizeHttpRequests(auth -> auth
  // .requestMatchers("/admin/**").hasRole("ADMIN")
  // .requestMatchers("/profesor/**").hasRole("PROFESOR")
  // .requestMatchers("/alumno/**").hasRole("ALUMNO")
  // .requestMatchers("/", "/login", "/css/**", "/js/**",
  // "/images/**").permitAll()
  // .anyRequest().authenticated())
  // .formLogin(form -> form
  // .loginPage("/login")
  // .permitAll())
  // .logout(logout -> logout
  // .permitAll());
  //
  // return http.build();
  // }
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth
                    // 2. Roles específicos
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/profe/**")
                    .hasRole("PROFESOR")
                    .requestMatchers("/alumno/**")
                    .hasRole("ALUMNO")

                    // 3. Rutas públicas
                    .requestMatchers(
                        "/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/error")
                    .permitAll()

                    // 4. El resto requiere estar logueado
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(successHandler)
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login?logout")
                    .permitAll());

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
