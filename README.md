# isw2-g6-backend · Liga Amateur Santafesina

Backend del proyecto integrador de Ingeniería de Software II × Programación (IES N.º 9233).
API REST en Spring Boot 3 + Java 17 + H2 que consume la app móvil (`isw2-g6-app`, React Native con Expo).

**Grupo 6:** Juan Igancio Miretti · Juan Cruz Scolari · Matías Espino · Bautista Joaquin

## Cómo levantarlo desde cero (RNF-09)

Requisitos: **JDK 17** (no hace falta instalar Gradle, viene el wrapper).

```bash
git clone https://github.com/bautijoaquin/isw2-g6-backend.git
cd isw2-g6-backend
./gradlew bootRun        # en Windows (cmd/PowerShell): gradlew.bat bootRun
```

- API: http://localhost:8080
- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/liga_amateur`, usuario `sa`, sin contraseña)
- Usuario inicial: `admin` / `Admin2026!`
- La base se **recrea en cada arranque** (`spring.sql.init.mode=always`). Para conservar datos, cambiarlo a `never`.

Tests: `./gradlew test`

## Estructura (RNF-04)

```
src/main/java/com/isw2/liga/
├── config/       SecurityConfig: qué rutas son públicas, CORS, BCrypt
├── controller/   Endpoints REST: solo reciben y devuelven DTOs
├── dto/          Records de entrada/salida (nunca se devuelven entidades)
├── exception/    ApiException + GlobalExceptionHandler → {status, mensaje}
├── model/        Entidades JPA (mapean schema.sql, ddl-auto=none)
├── repository/   Interfaces Spring Data JPA
├── security/     JwtService, JwtAuthFilter, UsuarioAutenticado
└── service/      Reglas de negocio (RN-xx) y transacciones
```

### Convenciones para sumar un módulo
- Las reglas de negocio van en el **service**, nunca en el controller.
- Para cortar con un error: `throw ApiException.conflicto("La cancha ya tiene un partido a menos de 2 horas (RN-01)")`.
  También están `noEncontrado` (404), `datosInvalidos` (400) y `noAutorizado` (401).
- Los permisos se marcan en el controller: `@PreAuthorize("hasRole('ADMIN')")` o `@PreAuthorize("hasAnyRole('ADMIN','DELEGADO')")`.
  Los GET bajo `/api/**` son públicos, salvo `/api/usuarios/**`, que es solo para ADMIN.
- Para saber quién llama: `@AuthenticationPrincipal UsuarioAutenticado usuario`.
- Validaciones de formato con `@Valid` + `@NotBlank`, `@Size`, `@Min`, `@Max` en los DTOs de entrada.

## Contrato de la API

| Método | Ruta | Quién | RF | Estado |
|---|---|---|---|---|
| POST | `/api/auth/login` | Público | RF-01 | ✅ |
| GET | `/api/torneos`, `/api/canchas`, `/api/arbitros` | Público | RF-05 | ✅ |
| GET/POST/PUT | `/api/usuarios`, `/api/usuarios/{id}` | Admin | RF-02, RN-08 | ✅ |
| PUT | `/api/usuarios/{id}/password` | Admin | RF-02 | ✅ |
| PATCH | `/api/usuarios/{id}/activo` | Admin | RF-02, RN-08 | ✅ |
| GET/POST/PUT | `/api/equipos`, `/api/equipos/{id}` | GET público, resto Admin | RF-03 | ✅ |
| PATCH | `/api/equipos/{id}/activo` | Admin | RF-03, RN-09 | ✅ |
| GET | `/api/equipos/{id}/jugadores`, `/api/jugadores/{id}` | Público | RF-05 | ✅ |
| POST/PUT/PATCH | `/api/jugadores`, `/api/jugadores/{id}`, `/api/jugadores/{id}/activo` | Admin | RF-04 | ✅ |
| GET | `/api/partidos?torneoId=1&fecha=3` | Público | RF-10 | ✅ |
| POST | `/api/partidos` | Admin | RF-06, RN-01, RN-02, RN-07 | ✅ |
| PUT/DELETE | `/api/partidos/{id}` | Admin | RF-06, RN-07 | ⬜ Scolari |
| POST | `/api/partidos/{id}/iniciar`, `/finalizar`, `/suspender` | Delegado/Admin | RF-07, RN-05 | ⬜ Scolari |
| GET | `/api/partidos/{id}` (detalle + eventos + resultado) | Público | RF-10, RN-06 | ✅ |
| POST | `/api/partidos/{id}/eventos` | Delegado/Admin | RF-08, RN-03, RN-04 | ✅ |
| DELETE | `/api/eventos/{id}` | Delegado/Admin | RF-09 | ✅ |
| GET | `/api/torneos/{id}/tabla` | Público | RF-11 | ✅ |
| GET | `/api/torneos/{id}/fechas/{n}/suspendidos` | Público | RF-12, RN-03 | ✅ |

Errores: siempre `{"status": 409, "mensaje": "..."}`. Sin token: 401. Sin el rol: 403.

### Ejemplos de cuerpos (usuarios)

```jsonc
// POST /api/usuarios → 201
{ "username": "delegado1", "password": "clave1234", "nombreCompleto": "Juan Pérez", "rol": "DELEGADO" }
// PUT /api/usuarios/{id} → 200 (el username no se cambia)
{ "nombreCompleto": "Juan Pérez", "rol": "ADMIN" }
// PUT /api/usuarios/{id}/password → 204
{ "password": "nuevaClave1" }
// PATCH /api/usuarios/{id}/activo → 200 (también sirve para equipos y jugadores)
{ "activo": false }
```

`GET /api/usuarios` devuelve activos e inactivos; con `?incluirInactivos=false` solo los activos.

### Ejemplos de cuerpos (equipos y jugadores)

```jsonc
// POST /api/equipos → 201 (barrio opcional; nombre único dentro del torneo)
{ "torneoId": 1, "nombre": "Los Pumas", "barrio": "Candioti" }
// PUT /api/equipos/{id} → 200 (el torneo no se cambia)
{ "nombre": "Los Pumas FC", "barrio": "Candioti Sur" }
// POST /api/jugadores → 201 (DNI de 7 u 8 dígitos, único en todo el sistema; camiseta 1 a 99, opcional)
{ "equipoId": 1, "nombre": "Juan", "apellido": "Pérez", "dni": "35123456", "numeroCamiseta": 10 }
// PUT /api/jugadores/{id} → 200 (no trae equipoId: el jugador no cambia de equipo)
{ "nombre": "Juan", "apellido": "Pérez", "dni": "35123456", "numeroCamiseta": 9 }
```

`GET /api/equipos` acepta `?torneoId=1`. `GET /api/equipos` y `GET /api/equipos/{id}/jugadores` devuelven solo los activos;
con `?incluirInactivos=true` también los desactivados (RF-05). No se puede cargar un jugador en un equipo desactivado.

### Fixture (partidos)

`GET /api/partidos?torneoId=1&fecha=3` devuelve los partidos de esa fecha ordenados por horario, cada uno con
`local`, `visitante`, `cancha`, `arbitro`, `fechaHoraInicio`, `estado` y el resultado calculado (`golesLocal`, `golesVisitante`).
Sin `fecha` devuelve todo el torneo, ordenado por fecha y horario.

```jsonc
// POST /api/partidos → 201 (queda PROGRAMADO)
{ "torneoId": 1, "numeroFecha": 1, "equipoLocalId": 1, "equipoVisitanteId": 2,
  "canchaId": 1, "arbitroId": 1, "fechaHoraInicio": "2026-11-07T15:00" }
```

Al programar se controla:
- RF-06: local y visitante distintos, del mismo torneo y activos; cancha y árbitro activos; un equipo no juega dos veces en la misma fecha.
- RN-01: la cancha no puede tener otro partido que empiece a menos de 2 horas (con exactamente 2 horas, sí).
- RN-02: lo mismo para el árbitro.
- RN-07: las fechas se juegan en orden. Una fecha termina 2 horas después de que empieza su último partido:
  la fecha N no puede empezar antes de que termine la N-1, ni terminar después de que empiece la N+1.

Los partidos SUSPENDIDOS no cuentan para RF-06, RN-01, RN-02 ni RN-07 (un suspendido no se reprograma: se crea uno nuevo).

## Flujo de trabajo con Git

```
main      ← solo versiones estables (entregas y defensa)
  └── develop   ← integración de todo lo terminado
        └── feature/RF-xx-descripcion   ← una rama por funcionalidad
```

1. Arrancar cada funcionalidad **desde `develop`**:
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/RF-03-equipos
   ```
2. Commits con el código del requerimiento: `RF-08: valida jugador suspendido (RN-03)`.
3. Antes de abrir el PR: `git pull origin develop` (resolver conflictos en la rama propia) y `./gradlew test` en verde.
4. `git push -u origin feature/RF-03-equipos` y abrir el **Pull Request contra `develop`**, nunca contra `main`.
   Lo revisa al menos un compañero antes de mergear.
5. Cuando `develop` está estable y probado, se abre un **PR de `develop` a `main`**.

A `main` y `develop` no se hace push directo: todo entra por Pull Request.

El mismo flujo se usa en el repo de la app (`isw2-g6-app`), que es independiente: una funcionalidad
completa suele llevar un PR en cada repo, y el contrato de la API de este README es lo que los une.
