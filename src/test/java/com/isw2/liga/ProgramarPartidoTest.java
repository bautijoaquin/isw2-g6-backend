package com.isw2.liga;

import com.jayway.jsonpath.JsonPath;
import com.isw2.liga.model.*;
import com.isw2.liga.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** RF-06: programar partidos con RN-01, RN-02 y RN-07. Canchas 1 y 2 y árbitros 1 y 2 vienen de data.sql. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:liga_test;DB_CLOSE_DELAY=-1")
@Transactional
class ProgramarPartidoTest {

    @Autowired private MockMvc mvc;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private PartidoRepository partidoRepository;

    private String tokenAdmin;
    private Equipo norte, sur, oeste, este;

    @BeforeEach
    void cargarDatos() throws Exception {
        tokenAdmin = login("admin", "Admin2026!");
        norte = equipo("Norte");
        sur = equipo("Sur");
        oeste = equipo("Oeste");
        este = equipo("Este");
    }

    @Test
    void elAdminProgramaUnPartidoQueQuedaProgramado() throws Exception {
        programar(1, norte, sur, 1, 1, "2026-11-07T15:00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"))
                .andExpect(jsonPath("$.local.nombre").value("Norte"))
                .andExpect(jsonPath("$.visitante.nombre").value("Sur"))
                .andExpect(jsonPath("$.golesLocal").value(0))
                .andExpect(jsonPath("$.fechaHoraInicio").value("2026-11-07T15:00:00"));
    }

    @Test
    void laMismaCanchaAMenosDeDosHorasDevuelve409() throws Exception {
        programar(1, norte, sur, 1, 1, "2026-11-07T15:00").andExpect(status().isCreated());
        programar(1, oeste, este, 1, 2, "2026-11-07T16:59")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La cancha ya tiene un partido a menos de 2 horas (RN-01)"));
        // Justo dos horas después ya se puede
        programar(1, oeste, este, 1, 2, "2026-11-07T17:00").andExpect(status().isCreated());
    }

    @Test
    void unPartidoSuspendidoNoOcupaLaCancha() throws Exception {
        Integer id = JsonPath.read(programar(1, norte, sur, 1, 1, "2026-11-07T15:00")
                .andReturn().getResponse().getContentAsString(), "$.id");
        partidoRepository.findById(id).orElseThrow().setEstado(EstadoPartido.SUSPENDIDO);
        // Misma cancha, mismo árbitro y los mismos equipos en la misma fecha: el suspendido no cuenta
        programar(1, norte, sur, 1, 1, "2026-11-07T16:00").andExpect(status().isCreated());
    }

    @Test
    void unEquipoNoJuegaDosVecesEnLaMismaFecha() throws Exception {
        programar(1, norte, sur, 1, 1, "2026-11-07T15:00").andExpect(status().isCreated());
        programar(1, norte, oeste, 2, 2, "2026-11-08T15:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Norte ya juega en la fecha 1 (RN-02)"));
        // En otra fecha sí
        programar(2, norte, oeste, 2, 2, "2026-11-14T15:00").andExpect(status().isCreated());
    }

    @Test
    void elArbitroNoDirigeDosPartidosAMenosDeDosHoras() throws Exception {
        programar(1, norte, sur, 1, 1, "2026-11-07T15:00").andExpect(status().isCreated());
        programar(1, oeste, este, 2, 1, "2026-11-07T16:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El árbitro ya dirige un partido a menos de 2 horas (RN-02)"));
    }

    @Test
    void localYVisitanteIgualesDevuelve400() throws Exception {
        programar(1, norte, norte, 1, 1, "2026-11-07T15:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El equipo local y el visitante tienen que ser distintos (RN-07)"));
    }

    @Test
    void unEquipoDesactivadoNoSeProgramaDevuelve409() throws Exception {
        sur.setActivo(false);
        programar(1, norte, sur, 1, 1, "2026-11-07T15:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El equipo Sur está desactivado (RN-07)"));
    }

    @Test
    void datosQueNoExistenOIncompletos() throws Exception {
        mvc.perform(post("/api/partidos").header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, norte.getId(), sur.getId(), 99, 1, "2026-11-07T15:00")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/partidos").header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"torneoId\":1,\"numeroFecha\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinTokenDevuelve401YElDelegadoDevuelve403() throws Exception {
        String body = cuerpo(1, norte.getId(), sur.getId(), 1, 1, "2026-11-07T15:00");
        mvc.perform(post("/api/partidos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());

        Usuario delegado = new Usuario();
        delegado.setUsername("delegadoPartidos");
        delegado.setPasswordHash(passwordEncoder.encode("clave1234"));
        delegado.setNombreCompleto("Delegado");
        delegado.setRol(Rol.DELEGADO);
        usuarioRepository.save(delegado);
        mvc.perform(post("/api/partidos").header("Authorization", "Bearer " + login("delegadoPartidos", "clave1234"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    private ResultActions programar(int fecha, Equipo local, Equipo visitante, int canchaId, int arbitroId,
                                    String inicio) throws Exception {
        return mvc.perform(post("/api/partidos").header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo(fecha, local.getId(), visitante.getId(), canchaId, arbitroId, inicio)));
    }

    private static String cuerpo(int fecha, int localId, int visitanteId, int canchaId, int arbitroId, String inicio) {
        return "{\"torneoId\":1,\"numeroFecha\":" + fecha + ",\"equipoLocalId\":" + localId
                + ",\"equipoVisitanteId\":" + visitanteId + ",\"canchaId\":" + canchaId
                + ",\"arbitroId\":" + arbitroId + ",\"fechaHoraInicio\":\"" + inicio + "\"}";
    }

    private String login(String username, String password) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private Equipo equipo(String nombre) {
        Equipo e = new Equipo();
        e.setTorneo(torneoRepository.findById(1).orElseThrow());
        e.setNombre(nombre);
        return equipoRepository.save(e);
    }
}
