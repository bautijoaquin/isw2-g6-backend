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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** RF-06 y RN-07: modificar y eliminar partidos, solo mientras están PROGRAMADOS. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:liga_test;DB_CLOSE_DELAY=-1")
@Transactional
class ModificarEliminarPartidoTest {

    @Autowired private MockMvc mvc;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private PartidoRepository partidoRepository;

    private String tokenAdmin;
    private Equipo norte, sur, oeste, este;

    @BeforeEach
    void cargarDatos() throws Exception {
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin2026!\"}"))
                .andReturn().getResponse().getContentAsString();
        tokenAdmin = JsonPath.read(login, "$.token");
        norte = equipo("Norte");
        sur = equipo("Sur");
        oeste = equipo("Oeste");
        este = equipo("Este");
    }

    @Test
    void reprogramarCambiaHorarioYCancha() throws Exception {
        Integer id = programar(norte, sur, 1, "2026-11-07T15:00");
        modificar(id, norte, sur, 2, "2026-11-08T10:30")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancha.id").value(2))
                .andExpect(jsonPath("$.fechaHoraInicio").value("2026-11-08T10:30:00"))
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"));
    }

    @Test
    void guardarSinCambiosNoChocaConsigoMismo() throws Exception {
        Integer id = programar(norte, sur, 1, "2026-11-07T15:00");
        modificar(id, norte, sur, 1, "2026-11-07T15:30").andExpect(status().isOk());
    }

    @Test
    void reprogramarEncimaDeOtroPartidoDevuelve409() throws Exception {
        programar(norte, sur, 1, "2026-11-07T15:00");
        Integer otro = programar(oeste, este, 2, "2026-11-07T15:00");
        modificar(otro, oeste, este, 1, "2026-11-07T16:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La cancha ya tiene un partido a menos de 2 horas (RN-01)"));
    }

    @Test
    void unPartidoQueYaEmpezoNoSeModificaNiSeElimina() throws Exception {
        Integer id = programar(norte, sur, 1, "2026-11-07T15:00");
        partidoRepository.findById(id).orElseThrow().setEstado(EstadoPartido.EN_JUEGO);
        modificar(id, norte, sur, 1, "2026-11-07T18:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        "El partido está EN_JUEGO: solo se puede modificar un partido PROGRAMADO (RN-07)"));
        mvc.perform(delete("/api/partidos/" + id).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isConflict());
    }

    @Test
    void elTorneoDeUnPartidoNoSeCambia() throws Exception {
        Integer id = programar(norte, sur, 1, "2026-11-07T15:00");
        String body = cuerpo(norte, sur, 1, "2026-11-07T15:00").replace("\"torneoId\":1", "\"torneoId\":2");
        mvc.perform(put("/api/partidos/" + id).header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarUnPartidoProgramadoLoSacaDelFixture() throws Exception {
        Integer id = programar(norte, sur, 1, "2026-11-07T15:00");
        mvc.perform(delete("/api/partidos/" + id).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/partidos/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/partidos").param("torneoId", "1"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void partidoInexistenteYSinToken() throws Exception {
        modificar(9999, norte, sur, 1, "2026-11-07T15:00").andExpect(status().isNotFound());
        mvc.perform(delete("/api/partidos/9999").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
        Integer id = programar(norte, sur, 1, "2026-11-07T15:00");
        mvc.perform(delete("/api/partidos/" + id)).andExpect(status().isUnauthorized());
    }

    private Integer programar(Equipo local, Equipo visitante, int canchaId, String inicio) throws Exception {
        String respuesta = mvc.perform(post("/api/partidos").header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(local, visitante, canchaId, inicio)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.id");
    }

    private ResultActions modificar(Integer id, Equipo local, Equipo visitante, int canchaId, String inicio)
            throws Exception {
        return mvc.perform(put("/api/partidos/" + id).header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo(local, visitante, canchaId, inicio)));
    }

    /** Fecha 1 y el árbitro según la cancha, para que no choquen entre sí sin querer. */
    private static String cuerpo(Equipo local, Equipo visitante, int canchaId, String inicio) {
        return "{\"torneoId\":1,\"numeroFecha\":1,\"equipoLocalId\":" + local.getId()
                + ",\"equipoVisitanteId\":" + visitante.getId() + ",\"canchaId\":" + canchaId
                + ",\"arbitroId\":" + canchaId + ",\"fechaHoraInicio\":\"" + inicio + "\"}";
    }

    private Equipo equipo(String nombre) {
        Equipo e = new Equipo();
        e.setTorneo(torneoRepository.findById(1).orElseThrow());
        e.setNombre(nombre);
        return equipoRepository.save(e);
    }
}
