package com.isw2.liga;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** RF-03 Gestionar equipos, RF-04 Gestionar jugadores, RF-05 consulta pública y RN-09. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:liga_test;DB_CLOSE_DELAY=-1")
@Transactional
class EquipoJugadorControllerTest {

    private static final int ID_TORNEO = 1;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String tokenAdmin;

    @BeforeEach
    void loguearAdmin() throws Exception {
        tokenAdmin = login("admin", "Admin2026!");
    }

    // ---------- Equipos ----------

    @Test
    void altaDeEquipoConBarrioOpcional() throws Exception {
        crearEquipo("Los Pumas", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.torneoId").value(ID_TORNEO))
                .andExpect(jsonPath("$.nombre").value("Los Pumas"))
                .andExpect(jsonPath("$.barrio").doesNotExist())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void nombreRepetidoEnElTorneoDevuelve409() throws Exception {
        crearEquipo("Repetido FC", "Centro").andExpect(status().isCreated());
        crearEquipo("repetido fc", "Sur").andExpect(status().isConflict());
    }

    @Test
    void nombreVacioDevuelve400() throws Exception {
        crearEquipo("  ", null).andExpect(status().isBadRequest());
    }

    @Test
    void torneoInexistenteDevuelve404() throws Exception {
        String body = json.writeValueAsString(Map.of("torneoId", 9999, "nombre", "Sin torneo"));
        mvc.perform(post("/api/equipos").header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinTokenDevuelve401YDelegadoDevuelve403() throws Exception {
        String body = json.writeValueAsString(Map.of("torneoId", ID_TORNEO, "nombre", "Sin permiso"));
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", "delegadoEq", "password", "clave1234",
                        "nombreCompleto", "Delegado", "rol", "DELEGADO"))));
        String tokenDelegado = login("delegadoEq", "clave1234");
        mvc.perform(post("/api/equipos").header("Authorization", "Bearer " + tokenDelegado)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void modificarCambiaNombreYBarrio() throws Exception {
        int id = idDe(crearEquipo("Viejo Nombre", "Centro"));

        mvc.perform(put("/api/equipos/" + id).header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo Nombre\",\"barrio\":\"Candioti\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.barrio").value("Candioti"));
    }

    @Test
    void modificarConElNombreDeOtroEquipoDevuelve409() throws Exception {
        crearEquipo("Ocupado", null);
        int id = idDe(crearEquipo("Libre", null));

        mvc.perform(put("/api/equipos/" + id).header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Ocupado\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void equipoDesactivadoNoApareceEnLaListaPublicaSalvoQueSeLoPida() throws Exception {
        int id = idDe(crearEquipo("Dado de Baja", null));

        cambiarActivo("equipos", id, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(get("/api/equipos").param("torneoId", "" + ID_TORNEO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")]").isEmpty());
        mvc.perform(get("/api/equipos").param("incluirInactivos", "true"))
                .andExpect(jsonPath("$[?(@.id == " + id + ")]").isNotEmpty());
        mvc.perform(get("/api/equipos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void equipoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/equipos/9999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/equipos/9999/jugadores")).andExpect(status().isNotFound());
    }

    // ---------- Jugadores ----------

    @Test
    void altaDeJugadorYConsultaPublicaPorEquipo() throws Exception {
        int equipo = idDe(crearEquipo("Con Jugadores", null));

        crearJugador(equipo, "30000001", 10)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipoId").value(equipo))
                .andExpect(jsonPath("$.equipo").value("Con Jugadores"))
                .andExpect(jsonPath("$.dni").value("30000001"))
                .andExpect(jsonPath("$.numeroCamiseta").value(10))
                .andExpect(jsonPath("$.activo").value(true));

        mvc.perform(get("/api/equipos/" + equipo + "/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dni").value("30000001"));
    }

    @Test
    void dniRepetidoEnCualquierEquipoDevuelve409() throws Exception {
        int uno = idDe(crearEquipo("Equipo Uno", null));
        int dos = idDe(crearEquipo("Equipo Dos", null));

        crearJugador(uno, "30000002", 5).andExpect(status().isCreated());
        crearJugador(dos, "30000002", 7).andExpect(status().isConflict());
    }

    @Test
    void camisetaFueraDeRangoODniInvalidoDevuelve400() throws Exception {
        int equipo = idDe(crearEquipo("Validaciones", null));

        crearJugador(equipo, "30000003", 0).andExpect(status().isBadRequest());
        crearJugador(equipo, "30000003", 100).andExpect(status().isBadRequest());
        crearJugador(equipo, "30.000.003", 9).andExpect(status().isBadRequest());
    }

    @Test
    void noSePuedeCargarUnJugadorEnUnEquipoDesactivado() throws Exception {
        int equipo = idDe(crearEquipo("Inactivo", null));
        cambiarActivo("equipos", equipo, false);

        crearJugador(equipo, "30000004", 4).andExpect(status().isConflict());
    }

    @Test
    void modificarJugadorNoLoCambiaDeEquipo() throws Exception {
        int equipo = idDe(crearEquipo("Original", null));
        int otro = idDe(crearEquipo("Otro", null));
        int id = idDe(crearJugador(equipo, "30000005", 8));

        mvc.perform(put("/api/jugadores/" + id).header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipoId\":" + otro + ",\"nombre\":\"Lionel\",\"apellido\":\"Messi\","
                                + "\"dni\":\"30000005\",\"numeroCamiseta\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipoId").value(equipo))
                .andExpect(jsonPath("$.nombre").value("Lionel"))
                .andExpect(jsonPath("$.numeroCamiseta").value(10));
    }

    @Test
    void jugadorDesactivadoSoloApareceSiSePidenLosInactivos() throws Exception {
        int equipo = idDe(crearEquipo("Bajas", null));
        int id = idDe(crearJugador(equipo, "30000006", 6));

        cambiarActivo("jugadores", id, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(get("/api/equipos/" + equipo + "/jugadores"))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/equipos/" + equipo + "/jugadores").param("incluirInactivos", "true"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    private ResultActions crearEquipo(String nombre, String barrio) throws Exception {
        Map<String, Object> datos = new HashMap<>();
        datos.put("torneoId", ID_TORNEO);
        datos.put("nombre", nombre);
        datos.put("barrio", barrio);
        return mvc.perform(post("/api/equipos").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(datos)));
    }

    private ResultActions crearJugador(int equipoId, String dni, int camiseta) throws Exception {
        String body = json.writeValueAsString(Map.of("equipoId", equipoId, "nombre", "Juan",
                "apellido", "Pérez", "dni", dni, "numeroCamiseta", camiseta));
        return mvc.perform(post("/api/jugadores").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions cambiarActivo(String recurso, int id, boolean activo) throws Exception {
        return mvc.perform(patch("/api/" + recurso + "/" + id + "/activo").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":" + activo + "}"));
    }

    private String login(String username, String password) throws Exception {
        String body = json.writeValueAsString(Map.of("username", username, "password", password));
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("token").asText();
    }

    private int idDe(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString()).get("id").asInt();
    }
}
