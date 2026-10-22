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
// Cada test corre en una transacción que se deshace al final: los datos no se pisan entre tests
@Transactional
class ResultadosControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private CanchaRepository canchaRepository;
    @Autowired private ArbitroRepository arbitroRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidoRepository partidoRepository;
    @Autowired private EventoPartidoRepository eventoRepository;

    private Equipo norte, sur, oeste;
    private Jugador goleador, rival;

    @BeforeEach
    void cargarDatos() {
        norte = equipo("Norte");
        sur = equipo("Sur");
        oeste = equipo("Oeste");
        goleador = jugador(norte, "10000001");
        rival = jugador(sur, "10000002");
    }

    @Test
    void detalleDePartidoConResultadoCalculadoYEventosEnOrden() throws Exception {
        Partido p = partido(1, norte, sur, EstadoPartido.EN_JUEGO);
        evento(p, goleador, TipoEvento.GOL, 30);
        evento(p, rival, TipoEvento.GOL, 10);
        evento(p, goleador, TipoEvento.GOL, 75);

        mvc.perform(get("/api/partidos/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.local.nombre").value("Norte"))
                .andExpect(jsonPath("$.golesLocal").value(2))
                .andExpect(jsonPath("$.golesVisitante").value(1))
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"))
                .andExpect(jsonPath("$.eventos[0].minuto").value(10))
                .andExpect(jsonPath("$.eventos[2].minuto").value(75));
    }

    @Test
    void partidoSinGolesMuestraCeroACero() throws Exception {
        Partido p = partido(1, norte, sur, EstadoPartido.PROGRAMADO);
        mvc.perform(get("/api/partidos/" + p.getId()))
                .andExpect(jsonPath("$.golesLocal").value(0))
                .andExpect(jsonPath("$.golesVisitante").value(0));
    }

    @Test
    void partidoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/partidos/9999")).andExpect(status().isNotFound());
    }

    @Test
    void tablaSoloCuentaPartidosFinalizados() throws Exception {
        Partido jugado = partido(1, norte, sur, EstadoPartido.FINALIZADO);
        evento(jugado, goleador, TipoEvento.GOL, 20);
        Partido enJuego = partido(2, sur, norte, EstadoPartido.EN_JUEGO);
        evento(enJuego, rival, TipoEvento.GOL, 5);

        mvc.perform(get("/api/torneos/1/tabla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].equipo").value("Norte"))
                .andExpect(jsonPath("$[0].pts").value(3))
                .andExpect(jsonPath("$[0].pj").value(1))
                .andExpect(jsonPath("$[1].equipo").value("Oeste"))
                .andExpect(jsonPath("$[1].pj").value(0))
                .andExpect(jsonPath("$[2].equipo").value("Sur"))
                .andExpect(jsonPath("$[2].dg").value(-1));
    }

    @Test
    void torneoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/torneos/99/tabla")).andExpect(status().isNotFound());
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

    private Partido partido(int fecha, Equipo local, Equipo visitante, EstadoPartido estado) {
        Partido p = new Partido();
        p.setTorneo(torneoRepository.findById(1).orElseThrow());
        p.setNumeroFecha(fecha);
        p.setEquipoLocal(local);
        p.setEquipoVisitante(visitante);
        p.setCancha(canchaRepository.findById(1).orElseThrow());
        p.setArbitro(arbitroRepository.findById(1).orElseThrow());
        p.setFechaHoraInicio(LocalDateTime.of(2026, 11, fecha, 15, 0));
        p.setEstado(estado);
        return partidoRepository.save(p);
    }

    private void evento(Partido partido, Jugador jugador, TipoEvento tipo, int minuto) {
        EventoPartido e = new EventoPartido();
        e.setPartido(partido);
        e.setJugador(jugador);
        e.setTipo(tipo);
        e.setMinuto(minuto);
        eventoRepository.save(e);
    }
}
