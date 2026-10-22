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
| GET/POST/PUT | `/api/usuarios`, `/api/usuarios/{id}` | Admin | RF-02 | ⬜ Matías |
| PATCH | `/api/usuarios/{id}/activo` | Admin | RF-02, RN-08 | ⬜ Matías |
| GET/POST/PUT | `/api/equipos`, `/api/equipos/{id}` | GET público, resto Admin | RF-03 | ⬜ Miretti |
| PATCH | `/api/equipos/{id}/activo` | Admin | RF-03, RN-09 | ⬜ Miretti |
| GET | `/api/equipos/{id}/jugadores` | Público | RF-05 | ⬜ Miretti |
| POST/PUT/PATCH | `/api/jugadores`, `/api/jugadores/{id}`, `/api/jugadores/{id}/activo` | Admin | RF-04 | ⬜ Miretti |
| GET | `/api/partidos?torneoId=1&fecha=3` | Público | RF-10 | ⬜ Scolari |
| POST/PUT/DELETE | `/api/partidos`, `/api/partidos/{id}` | Admin | RF-06, RN-01, RN-02, RN-07 | ⬜ Scolari |
| POST | `/api/partidos/{id}/iniciar`, `/finalizar`, `/suspender` | Delegado/Admin | RF-07, RN-05 | ⬜ Scolari |
| GET | `/api/partidos/{id}` (detalle + eventos + resultado) | Público | RF-10, RN-06 | ✅ |
| POST | `/api/partidos/{id}/eventos` | Delegado/Admin | RF-08, RN-03, RN-04 | ⬜ Negro |
| DELETE | `/api/eventos/{id}` | Delegado/Admin | RF-09 | ⬜ Negro |
| GET | `/api/torneos/{id}/tabla` | Público | RF-11 | ✅ |
| GET | `/api/torneos/{id}/fechas/{n}/suspendidos` | Público | RF-12, RN-03 | ⬜ Negro |

Errores: siempre `{"status": 409, "mensaje": "..."}`. Sin token: 401. Sin el rol: 403.

## Flujo de trabajo con Git
- `main` siempre compila. Cada tarea va en su rama: `feature/RF-06-programar-partidos`.
- Commits con el código del requerimiento: `RF-08: valida jugador suspendido (RN-03)`.
- Se entra a `main` por Pull Request, revisado por al menos un compañero.
- Antes de abrir el PR: `./gradlew test` en verde.
