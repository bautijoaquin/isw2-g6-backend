package com.isw2.liga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** RF-02 Administrar usuarios y RN-08. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:liga_test;DB_CLOSE_DELAY=-1")
class UsuarioControllerTest {

    private static final int ID_ADMIN = 1;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String tokenAdmin;

    @BeforeEach
    void loguearAdmin() throws Exception {
        tokenAdmin = login("admin", "Admin2026!");
    }

    @Test
    void altaDeDelegadoNoDevuelveLaContrasenia() throws Exception {
        crear("delegado1", "clave1234", "DELEGADO")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("delegado1"))
                .andExpect(jsonPath("$.rol").value("DELEGADO"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.fechaAlta").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void usernameRepetidoDevuelve409() throws Exception {
        crear("repetido", "clave1234", "DELEGADO").andExpect(status().isCreated());
        crear("repetido", "otraClave99", "ADMIN").andExpect(status().isConflict());
    }

    @Test
    void contraseniaCortaDevuelve400() throws Exception {
        crear("corta", "1234567", "DELEGADO").andExpect(status().isBadRequest());
    }

    @Test
    void sinTokenDevuelve401YDelegadoDevuelve403() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());

        crear("delegado2", "clave1234", "DELEGADO");
        String tokenDelegado = login("delegado2", "clave1234");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenDelegado))
                .andExpect(status().isForbidden());
    }

    @Test
    void modificarCambiaNombreYRolPeroNoElUsername() throws Exception {
        int id = idDe(crear("modificable", "clave1234", "DELEGADO"));

        mvc.perform(put("/api/usuarios/" + id).header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"otro\",\"nombreCompleto\":\"Nuevo Nombre\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("modificable"))
                .andExpect(jsonPath("$.nombreCompleto").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void usuarioDesactivadoNoPuedeIngresarYReactivadoSi() throws Exception {
        int id = idDe(crear("baja", "clave1234", "DELEGADO"));

        cambiarActivo(id, false).andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));
        loginStatus("baja", "clave1234").andExpect(status().isUnauthorized());

        cambiarActivo(id, true).andExpect(status().isOk());
        loginStatus("baja", "clave1234").andExpect(status().isOk());
    }

    @Test
    void cambiarContraseniaPermiteIngresarConLaNueva() throws Exception {
        int id = idDe(crear("cambioclave", "clave1234", "DELEGADO"));

        mvc.perform(put("/api/usuarios/" + id + "/password").header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"nuevaClave1\"}"))
                .andExpect(status().isNoContent());

        loginStatus("cambioclave", "clave1234").andExpect(status().isUnauthorized());
        loginStatus("cambioclave", "nuevaClave1").andExpect(status().isOk());
    }

    @Test
    void adminNoPuedeDesactivarseNiQuitarseElRol() throws Exception {
        cambiarActivo(ID_ADMIN, false)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No puede desactivarse a sí mismo (RN-08)"));

        mvc.perform(put("/api/usuarios/" + ID_ADMIN).header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"Admin\",\"rol\":\"DELEGADO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void usuarioInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/usuarios/9999").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    private ResultActions crear(String username, String password, String rol) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of(
                "username", username, "password", password, "nombreCompleto", "Nombre de " + username, "rol", rol));
        return mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions cambiarActivo(int id, boolean activo) throws Exception {
        return mvc.perform(patch("/api/usuarios/" + id + "/activo").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":" + activo + "}"));
    }

    private ResultActions loginStatus(String username, String password) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of("username", username, "password", password));
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String login(String username, String password) throws Exception {
        String respuesta = loginStatus(username, password).andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("token").asText();
    }

    private int idDe(ResultActions resultado) throws Exception {
        JsonNode nodo = json.readTree(resultado.andReturn().getResponse().getContentAsString());
        return nodo.get("id").asInt();
    }
}
