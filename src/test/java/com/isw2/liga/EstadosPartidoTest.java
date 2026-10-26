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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** RF-07 y RN-05: iniciar, finalizar y suspender partidos. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:liga_test;DB_CLOSE_DELAY=-1")
@Transactional
class EstadosPartidoTest {

    @Autowired private MockMvc mvc;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private CanchaRepository canchaRepository;
    @Autowired private ArbitroRepository arbitroRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidoRepository partidoRepository;

    private String tokenDelegado;
    private Equipo norte, sur;

    @BeforeEach
    void cargarDatos() throws Exception {
        Usuario delegado = new Usuario();
        delegado.setUsername("delegadoEstados");
        delegado.setPasswordHash(passwordEncoder.encode("clave1234"));
        delegado.setNombreCompleto("Delegado");
        delegado.setRol(Rol.DELEGADO);
        usuarioRepository.save(delegado);
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"delegadoEstados\",\"password\":\"clave1234\"}"))
                .andReturn().getResponse().getContentAsString();
        tokenDelegado = JsonPath.read(login, "$.token");
        norte = equipo("Norte");
        sur = equipo("Sur");
    }

    @Test
    void partidoCompletoDesdeQueEmpiezaHastaQueSumaEnLaTabla() throws Exception {
        Partido p = partido(EstadoPartido.PROGRAMADO);
        Jugador goleador = jugador(norte, "40000001");

        cambiar(p, "iniciar").andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("EN_JUEGO"));
        mvc.perform(post("/api/partidos/" + p.getId() + "/eventos").header("Authorization", "Bearer " + tokenDelegado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jugadorId\":" + goleador.getId() + ",\"tipo\":\"GOL\",\"minuto\":12}"))
                .andExpect(status().isCreated());
        cambiar(p, "finalizar")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.golesLocal").value(1));

        mvc.perform(get("/api/torneos/1/tabla"))
                .andExpect(jsonPath("$[0].equipo").value("Norte"))
                .andExpect(jsonPath("$[0].pts").value(3));
    }

    @Test
    void seSuspendeUnPartidoProgramadoOEnJuego() throws Exception {
        cambiar(partido(EstadoPartido.PROGRAMADO), "suspender")
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("SUSPENDIDO"));
        cambiar(partido(EstadoPartido.EN_JUEGO), "suspender")
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("SUSPENDIDO"));
    }

    @Test
    void lasTransicionesQueNoSonDeRn05Devuelven409() throws Exception {
        cambiar(partido(EstadoPartido.PROGRAMADO), "finalizar")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Un partido PROGRAMADO no puede pasar a FINALIZADO (RN-05)"));
        cambiar(partido(EstadoPartido.FINALIZADO), "iniciar").andExpect(status().isConflict());
        cambiar(partido(EstadoPartido.SUSPENDIDO), "iniciar").andExpect(status().isConflict());
        cambiar(partido(EstadoPartido.EN_JUEGO), "iniciar").andExpect(status().isConflict());
    }

    @Test
    void sinTokenDevuelve401YPartidoInexistente404() throws Exception {
        Partido p = partido(EstadoPartido.PROGRAMADO);
        mvc.perform(post("/api/partidos/" + p.getId() + "/iniciar")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/partidos/9999/iniciar").header("Authorization", "Bearer " + tokenDelegado))
                .andExpect(status().isNotFound());
    }

    private ResultActions cambiar(Partido p, String accion) throws Exception {
        return mvc.perform(post("/api/partidos/" + p.getId() + "/" + accion)
                .header("Authorization", "Bearer " + tokenDelegado));
    }

    private Equipo equipo(String nombre) {
        Equipo e = new Equipo();
        e.setTorneo(torneoRepository.findById(1).orElseThrow());
        e.setNombre(nombre);
        return equipoRepository.save(e);
    }

    private Jugador jugador(Equipo equipo, String dni) {
        Jugador j = new Jugador();
        j.setEquipo(equipo);
        j.setNombre("Jugador");
        j.setApellido(dni);
        j.setDni(dni);
        return jugadorRepository.save(j);
    }

    private Partido partido(EstadoPartido estado) {
        Partido p = new Partido();
        p.setTorneo(torneoRepository.findById(1).orElseThrow());
        p.setNumeroFecha(1);
        p.setEquipoLocal(norte);
        p.setEquipoVisitante(sur);
        p.setCancha(canchaRepository.findById(1).orElseThrow());
        p.setArbitro(arbitroRepository.findById(1).orElseThrow());
        p.setFechaHoraInicio(LocalDateTime.of(2026, 11, 7, 15, 0));
        p.setEstado(estado);
        return partidoRepository.save(p);
    }
}
