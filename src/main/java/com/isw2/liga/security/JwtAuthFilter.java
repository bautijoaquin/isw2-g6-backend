package com.isw2.liga.security;

import com.isw2.liga.model.Usuario;
import com.isw2.liga.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el header "Authorization: Bearer <token>" y, si el token es válido,
 * deja autenticado al usuario con su rol (ROLE_ADMIN o ROLE_DELEGADO).
 * El rol y si está activo se leen de la base en cada petición: un usuario desactivado
 * pierde el acceso aunque su token no haya vencido, y un cambio de rol se aplica enseguida (RF-02).
 * Si no hay token, es inválido o el usuario está desactivado, la petición sigue como anónima y
 * SecurityConfig decide si corresponde 401.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIJO)) {
            jwtService.validar(header.substring(PREFIJO.length()))
                    .map(claims -> claims.get("id", Integer.class))
                    .flatMap(usuarioRepository::findById)
                    .filter(Usuario::isActivo)
                    .ifPresent(usuario -> {
                        String rol = usuario.getRol().name();
                        var principal = new UsuarioAutenticado(usuario.getId(), usuario.getUsername(), rol);
                        var auth = new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }
}
