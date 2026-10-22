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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:liga_test;DB_CLOSE_DELAY=-1")
@Transactional
class EventosControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private CanchaRepository canchaRepository;
    @Autowired private ArbitroRepository arbitroRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidoRepository partidoRepository;
    @Autowired private EventoPartidoRepository eventoRepository;

    private String token;
    private Equipo norte, sur, oeste;
    private Jugador delNorte, delSur, deOtroEquipo;

    @BeforeEach
    void cargarDatos() throws Exception {
        Usuario delegado = new Usuario();
        delegado.setUsername("delegado");
        delegado.setPasswordHash(passwordEncoder.encode("Delegado2026!"));
        delegado.setNombreCompleto("Delegado de prueba");
        delegado.setRol(Rol.DELEGADO);
        usuarioRepository.save(delegado);
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"delegado\",\"password\":\"Delegado2026!\"}"))
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(login, "$.token");

        norte = equipo("Norte");
        sur = equipo("Sur");
        oeste = equipo("Oeste");
        delNorte = jugador(norte, "20000001");
        delSur = jugador(sur, "20000002");
        deOtroEquipo = jugador(oeste, "20000003");
    }

    @Test
    void elDelegadoCargaUnGolYElResultadoLoSuma() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        cargar(p, delNorte, "GOL", 23)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tipo").value("GOL"))
                .andExpect(jsonPath("$.equipoId").value(norte.getId()));
        mvc.perform(get("/api/partidos/" + p.getId()))
                .andExpect(jsonPath("$.golesLocal").value(1))
                .andExpect(jsonPath("$.eventos.length()").value(1));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        mvc.perform(post("/api/partidos/" + p.getId() + "/eventos").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(delNorte, "GOL", 10)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conElPartidoProgramadoOFinalizadoNoSeCargaNada() throws Exception {
        cargar(partido(1, EstadoPartido.PROGRAMADO), delNorte, "GOL", 10).andExpect(status().isConflict());
        cargar(partido(1, EstadoPartido.FINALIZADO), delNorte, "GOL", 10).andExpect(status().isConflict());
    }

    @Test
    void unJugadorDeOtroEquipoNoPuedeTenerEventos() throws Exception {
        cargar(partido(1, EstadoPartido.EN_JUEGO), deOtroEquipo, "GOL", 10)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El jugador no es del equipo local ni del visitante (RN-04)"));
    }

    @Test
    void unJugadorDadoDeBajaNoPuedeTenerEventos() throws Exception {
        delSur.setActivo(false);
        cargar(partido(1, EstadoPartido.EN_JUEGO), delSur, "AMARILLA", 10).andExpect(status().isBadRequest());
    }

    @Test
    void unJugadorSuspendidoNoPuedeTenerEventos() throws Exception {
        // Roja en la fecha 1 (partido finalizado): queda suspendido para la fecha 2
        Partido fecha1 = partido(1, EstadoPartido.FINALIZADO);
        evento(fecha1, delSur, TipoEvento.ROJA, 50);

        Partido fecha2 = partido(2, EstadoPartido.EN_JUEGO);
        cargar(fecha2, delSur, "GOL", 10)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El jugador está suspendido para la fecha 2 (RN-03)"));
        // El resto del equipo juega normal
        cargar(fecha2, delNorte, "GOL", 11).andExpect(status().isCreated());
    }

    @Test
    void minutoFueraDeRangoOTipoInvalidoDevuelven400() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        cargar(p, delNorte, "GOL", 0).andExpect(status().isBadRequest());
        cargar(p, delNorte, "GOL", 121).andExpect(status().isBadRequest());
        cargar(p, delNorte, "PENAL", 10).andExpect(status().isBadRequest());
    }

    @Test
    void partidoInexistenteDevuelve404() throws Exception {
        mvc.perform(post("/api/partidos/9999/eventos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(delNorte, "GOL", 10)))
                .andExpect(status().isNotFound());
    }

    @Test
    void borrarUnEventoConElPartidoEnJuegoLoSacaDelResultado() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        Integer id = evento(p, delNorte, TipoEvento.GOL, 30);
        mvc.perform(delete("/api/eventos/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/partidos/" + p.getId()))
                .andExpect(jsonPath("$.golesLocal").value(0))
                .andExpect(jsonPath("$.eventos.length()").value(0));
    }

    @Test
    void conElPartidoFinalizadoLosEventosNoSeBorran() throws Exception {
        Integer id = evento(partido(1, EstadoPartido.FINALIZADO), delNorte, TipoEvento.GOL, 30);
        mvc.perform(delete("/api/eventos/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void borrarSinTokenOUnEventoInexistente() throws Exception {
        Integer id = evento(partido(1, EstadoPartido.EN_JUEGO), delNorte, TipoEvento.GOL, 30);
        mvc.perform(delete("/api/eventos/" + id)).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/eventos/9999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private ResultActions cargar(Partido p, Jugador j, String tipo, int minuto) throws Exception {
        return mvc.perform(post("/api/partidos/" + p.getId() + "/eventos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo(j, tipo, minuto)));
    }

    private static String cuerpo(Jugador j, String tipo, int minuto) {
        return "{\"jugadorId\":" + j.getId() + ",\"tipo\":\"" + tipo + "\",\"minuto\":" + minuto + "}";
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

    /** Siempre Norte (local) contra Sur (visitante). */
    private Partido partido(int fecha, EstadoPartido estado) {
        Partido p = new Partido();
        p.setTorneo(torneoRepository.findById(1).orElseThrow());
        p.setNumeroFecha(fecha);
        p.setEquipoLocal(norte);
        p.setEquipoVisitante(sur);
        p.setCancha(canchaRepository.findById(1).orElseThrow());
        p.setArbitro(arbitroRepository.findById(1).orElseThrow());
        p.setFechaHoraInicio(LocalDateTime.of(2026, 11, fecha, 15, 0));
        p.setEstado(estado);
        return partidoRepository.save(p);
    }

    private Integer evento(Partido partido, Jugador jugador, TipoEvento tipo, int minuto) {
        EventoPartido e = new EventoPartido();
        e.setPartido(partido);
        e.setJugador(jugador);
        e.setTipo(tipo);
        e.setMinuto(minuto);
        return eventoRepository.save(e).getId();
    }
}
