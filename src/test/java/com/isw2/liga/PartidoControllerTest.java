package com.isw2.liga;

import com.isw2.liga.model.*;
import com.isw2.liga.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:liga_test;DB_CLOSE_DELAY=-1")
@Transactional
class PartidoControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private CanchaRepository canchaRepository;
    @Autowired private ArbitroRepository arbitroRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidoRepository partidoRepository;
    @Autowired private EventoPartidoRepository eventoRepository;

    private Equipo norte, sur, oeste, este;

    @BeforeEach
    void cargarDatos() {
        norte = equipo("Norte");
        sur = equipo("Sur");
        oeste = equipo("Oeste");
        este = equipo("Este");
    }

    @Test
    void elFixtureDeUnaFechaSaleOrdenadoPorHorarioYConResultado() throws Exception {
        Partido tarde = partido(1, norte, sur, LocalDateTime.of(2026, 11, 7, 18, 0), EstadoPartido.FINALIZADO);
        partido(1, oeste, este, LocalDateTime.of(2026, 11, 7, 15, 0), EstadoPartido.PROGRAMADO);
        partido(2, norte, oeste, LocalDateTime.of(2026, 11, 14, 15, 0), EstadoPartido.PROGRAMADO);
        gol(tarde, jugador(sur, "30000001"), 40);

        mvc.perform(get("/api/partidos").param("torneoId", "1").param("fecha", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].local.nombre").value("Oeste"))
                .andExpect(jsonPath("$[0].estado").value("PROGRAMADO"))
                .andExpect(jsonPath("$[1].local.nombre").value("Norte"))
                .andExpect(jsonPath("$[1].golesLocal").value(0))
                .andExpect(jsonPath("$[1].golesVisitante").value(1))
                .andExpect(jsonPath("$[1].cancha.nombre").value("Cancha Municipal Norte"))
                .andExpect(jsonPath("$[1].arbitro.nombre").value("Marcelo Quiroga"));
    }

    @Test
    void sinFechaDevuelveTodoElTorneoOrdenadoPorFecha() throws Exception {
        partido(2, norte, oeste, LocalDateTime.of(2026, 11, 14, 15, 0), EstadoPartido.PROGRAMADO);
        partido(1, norte, sur, LocalDateTime.of(2026, 11, 7, 15, 0), EstadoPartido.PROGRAMADO);

        mvc.perform(get("/api/partidos").param("torneoId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numeroFecha").value(1))
                .andExpect(jsonPath("$[1].numeroFecha").value(2));
    }

    @Test
    void unaFechaSinPartidosDevuelveUnaListaVacia() throws Exception {
        mvc.perform(get("/api/partidos").param("torneoId", "1").param("fecha", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void torneoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/partidos").param("torneoId", "9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinTorneoOConFechaCeroDevuelve400() throws Exception {
        mvc.perform(get("/api/partidos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parámetro torneoId"));
        mvc.perform(get("/api/partidos").param("torneoId", "1").param("fecha", "0"))
                .andExpect(status().isBadRequest());
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

    private Partido partido(int fecha, Equipo local, Equipo visitante, LocalDateTime inicio, EstadoPartido estado) {
        Partido p = new Partido();
        p.setTorneo(torneoRepository.findById(1).orElseThrow());
        p.setNumeroFecha(fecha);
        p.setEquipoLocal(local);
        p.setEquipoVisitante(visitante);
        p.setCancha(canchaRepository.findById(1).orElseThrow());
        p.setArbitro(arbitroRepository.findById(1).orElseThrow());
        p.setFechaHoraInicio(inicio);
        p.setEstado(estado);
        return partidoRepository.save(p);
    }

    private void gol(Partido p, Jugador j, int minuto) {
        EventoPartido e = new EventoPartido();
        e.setPartido(p);
        e.setJugador(j);
        e.setTipo(TipoEvento.GOL);
        e.setMinuto(minuto);
        eventoRepository.save(e);
    }
}
