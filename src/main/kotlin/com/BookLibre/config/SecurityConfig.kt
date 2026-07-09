package com.BookLibre.config

import com.BookLibre.security.JwtAuthFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .authorizeHttpRequests { auth ->
                auth
                    // Rutas públicas — no requieren token
                    .requestMatchers("/api/user/login").permitAll()
                    .requestMatchers("/api/user/register").permitAll()
                    .requestMatchers("/api/user/refresh").permitAll()
                    .requestMatchers("/health").permitAll()
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/api/activity/sse").permitAll()

                    //para testear con postman
                    .requestMatchers("/graphiql", "/graphiql/**").permitAll()
                    .requestMatchers("/graphql", "/graphql/**").permitAll()
                    .requestMatchers("/vendor/**").permitAll()

                    // role filter
                    .requestMatchers(HttpMethod.POST, "/api/book/search").authenticated()

                    .requestMatchers(HttpMethod.POST, "/api/book/user/{userId}").hasAuthority("PUBLISHER")
                    .requestMatchers(HttpMethod.DELETE, "/api/book/{id}").hasAuthority("PUBLISHER")
                    .requestMatchers(HttpMethod.GET, "/api/book/getUpdateById/{id}").hasAuthority("PUBLISHER")
                    .requestMatchers(HttpMethod.PUT, "/api/book/update/{id}").hasAuthority("PUBLISHER")


                    // Todo lo demás requiere autenticación
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                // 401 para cuando no hay token
                it.authenticationEntryPoint { _, response, authException ->
                    response.contentType = "application/json"
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.writer.write("""{"error":"UNAUTHORIZED","message":"${authException.message}"}""")
                }
                // 403 cuando no hay rol suficiente
                it.accessDeniedHandler { _, response, accessDeniedException ->
                    response.contentType = "application/json"
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.writer.write("""{"error":"FORBIDDEN","message":"${accessDeniedException.message}"}""")
                }
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}