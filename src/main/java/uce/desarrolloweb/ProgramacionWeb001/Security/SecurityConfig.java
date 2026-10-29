package uce.desarrolloweb.ProgramacionWeb001.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desactivamos CSRF porque es una API REST (si usas formularios, quizás quieras dejarlo)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Aquí le decimos que TODAS las rutas requieren autenticación
                        .anyRequest().authenticated()
                )
                // Agregamos nuestro filtro personalizado antes del filtro por defecto de Spring
                .addFilterBefore(new ApiKeyFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}