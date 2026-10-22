package com.isw2.liga.service;

import com.isw2.liga.dto.UsuarioCrearRequest;
import com.isw2.liga.dto.UsuarioDTO;
import com.isw2.liga.dto.UsuarioModificarRequest;
import com.isw2.liga.exception.ApiException;
import com.isw2.liga.model.Rol;
import com.isw2.liga.model.Usuario;
import com.isw2.liga.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** RF-02 Administrar usuarios. */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          EntityManager entityManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar(boolean incluirInactivos) {
        return usuarioRepository.findAll(Sort.by("username")).stream()
                .filter(u -> incluirInactivos || u.isActivo())
                .map(UsuarioDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtener(Integer id) {
        return UsuarioDTO.desde(buscar(id));
    }

    public UsuarioDTO crear(UsuarioCrearRequest request) {
        String username = request.username().trim();
        if (usuarioRepository.existsByUsername(username)) {
            throw ApiException.conflicto("Ya existe un usuario con el nombre " + username);
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setRol(request.rol());

        usuarioRepository.saveAndFlush(usuario);
        // fecha_alta la completa la base: se relee para devolverla
        entityManager.refresh(usuario);
        return UsuarioDTO.desde(usuario);
    }

    public UsuarioDTO modificar(Integer id, UsuarioModificarRequest request, Integer idQuienModifica) {
        Usuario usuario = buscar(id);
        // RN-08: un administrador no puede quitarse a sí mismo el rol de administrador
        if (esElMismo(usuario, idQuienModifica) && request.rol() != Rol.ADMIN) {
            throw ApiException.conflicto("No puede quitarse a sí mismo el rol de administrador (RN-08)");
        }
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setRol(request.rol());
        return UsuarioDTO.desde(usuario);
    }

    public void cambiarPassword(Integer id, String passwordNueva) {
        buscar(id).setPasswordHash(passwordEncoder.encode(passwordNueva));
    }

    public UsuarioDTO cambiarActivo(Integer id, boolean activo, Integer idQuienModifica) {
        Usuario usuario = buscar(id);
        // RN-08: un administrador no puede desactivarse a sí mismo
        if (!activo && esElMismo(usuario, idQuienModifica)) {
            throw ApiException.conflicto("No puede desactivarse a sí mismo (RN-08)");
        }
        usuario.setActivo(activo);
        return UsuarioDTO.desde(usuario);
    }

    private Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el usuario " + id));
    }

    private boolean esElMismo(Usuario usuario, Integer idQuienModifica) {
        return Objects.equals(usuario.getId(), idQuienModifica);
    }
}
