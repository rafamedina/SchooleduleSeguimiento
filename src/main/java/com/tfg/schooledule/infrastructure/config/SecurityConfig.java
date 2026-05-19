package com.tfg.schooledule.infrastructure.config;

import com.tfg.schooledule.infrastructure.security.LoginRateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private static final String LOGIN_URL = "/login";

  private final LoginRateLimitFilter loginRateLimitFilter;
  private final CustomLoginSuccessHandler successHandler;

  public SecurityConfig(
      LoginRateLimitFilter loginRateLimitFilter, CustomLoginSuccessHandler successHandler) {
    this.loginRateLimitFilter = loginRateLimitFilter;
    this.successHandler = successHandler;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable()) // NOSONAR: Swagger UI is stateless; CSRF not applicable
        .headers(
            headers ->
                headers
                    .frameOptions(frame -> frame.sameOrigin())
                    .contentSecurityPolicy(
                        csp ->
                            csp.policyDirectives(
                                "default-src 'self'; "
                                    + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
                                    + "style-src 'self' 'unsafe-inline'; "
                                    + "img-src 'self' data: blob:; "
                                    + "font-src 'self' data:; "
                                    + "connect-src 'self';")));
    return http.build();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/snap-admin/**")
                    .denyAll()
                    .requestMatchers("/centro-admin/**")
                    .hasRole("ADMIN_CENTRO")
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/profe/**", "/tutor/**")
                    .hasRole("PROFESOR")
                    .requestMatchers("/alumno/**")
                    .hasRole("ALUMNO")
                    .requestMatchers(
                        "/",
                        LOGIN_URL,
                        "/register",
                        "/change-password",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage(LOGIN_URL)
                    .loginProcessingUrl(LOGIN_URL)
                    .successHandler(successHandler)
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login?logout")
                    .permitAll())
        .sessionManagement(
            session ->
                session
                    .sessionFixation()
                    .changeSessionId()
                    .maximumSessions(1)
                    .expiredUrl("/login?expired"))
        .headers(
            headers ->
                headers
                    .contentSecurityPolicy(
                        csp ->
                            csp.policyDirectives(
                                "default-src 'self'; "
                                    + "script-src 'self' https://cdn.jsdelivr.net; "
                                    + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                    + "img-src 'self' data:; "
                                    + "font-src 'self'; "
                                    + "frame-ancestors 'none'; "
                                    + "object-src 'none'"))
                    .addHeaderWriter(
                        new StaticHeadersWriter(
                            "Referrer-Policy", "strict-origin-when-cross-origin"))
                    .addHeaderWriter(
                        new StaticHeadersWriter(
                            "Permissions-Policy", "camera=(), microphone=(), geolocation=()")));

    return http.build();
  }

  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
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
