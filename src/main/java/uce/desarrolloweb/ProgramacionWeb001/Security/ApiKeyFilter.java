package uce.desarrolloweb.ProgramacionWeb001.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class ApiKeyFilter extends OncePerRequestFilter {

    // Definimos la clave secreta
    private static final String API_KEY = "MiClaveSuperSecreta123";
    private static final String API_KEY_HEADER = "X-API-KEY";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Obtenemos el valor del header
        String requestApiKey = request.getHeader(API_KEY_HEADER);

        // Si la clave no coincide, bloqueamos la petición
        if (API_KEY.equals(requestApiKey)) {
            // La clave es correcta, dejamos que la petición siga su curso
            filterChain.doFilter(request, response);
        } else {
            // La clave es incorrecta o no existe
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Acceso denegado. Falta la API Key o es incorrecta.\"}");
        }
    }
}