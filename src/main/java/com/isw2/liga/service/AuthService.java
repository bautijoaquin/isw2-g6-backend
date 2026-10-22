package com.isw2.liga.service;

import com.isw2.liga.dto.LoginRequest;
import com.isw2.liga.dto.LoginResponse;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.Usuario;
import com.isw2.liga.repository.UsuarioRepository;
import com.isw2.liga.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-01 Iniciar sesión. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // Mismo mensaje para usuario inexistente, inactivo o contraseña incorrecta:
        // no se aclara qué dato falló
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .filter(Usuario::isActivo)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.noAutorizado("Usuario o contraseña incorrectos"));

        return new LoginResponse(jwtService.generarToken(usuario), usuario.getNombreCompleto(), usuario.getRol());
    }
}
