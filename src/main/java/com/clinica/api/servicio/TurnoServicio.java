package com.clinica.api.servicio;

import com.clinica.api.modelo.documento.TurnoDto;
import com.clinica.api.modelo.documento.enums.Rol;
import com.clinica.api.modelo.documento.enums.TurnoTipo;
import com.clinica.api.modelo.request.TurnoRequest;
import com.clinica.api.repositorio.TurnoRepositorio;
import com.clinica.api.repositorio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class TurnoServicio {

    private final TurnoRepositorio repositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    // ── Crear turno ──────────────────────────────────────────────────────────
    public Mono<TurnoDto> crearTurno(TurnoRequest request) {
        return usuarioRepositorio.findById(request.getUsuarioId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + request.getUsuarioId())))
                .flatMap(usuario -> {
                    // Solo médicos y enfermeros pueden tener turnos
                    if (usuario.getRol() != Rol.MEDICO && usuario.getRol() != Rol.ENFERMERO) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Solo se pueden asignar turnos a usuarios con rol MEDICO o ENFERMERO"));
                    }
                    if (!usuario.isActivo()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "No se puede asignar turno a un usuario inactivo"));
                    }

                    // Verificar que no tenga turno duplicado ese día
                    return repositorio.existsByUsuarioIdAndFechaAndTipo(
                                    request.getUsuarioId(), request.getFecha(), request.getTipo())
                            .flatMap(existe -> {
                                if (Boolean.TRUE.equals(existe)) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                                            "El usuario ya tiene un turno " + request.getTipo()
                                                    + " asignado el " + request.getFecha()));
                                }

                                LocalTime[] horas = horasPorTipo(request.getTipo());
                                String area = request.getArea() != null
                                        ? request.getArea()
                                        : usuario.getAreaAsignada();

                                TurnoDto turno = TurnoDto.builder()
                                        .usuarioId(request.getUsuarioId())
                                        .usuarioNombre(usuario.getNombre() + " " + usuario.getApellido())
                                        .tipo(request.getTipo())
                                        .fecha(request.getFecha())
                                        .horaInicio(horas[0])
                                        .horaFin(horas[1])
                                        .area(area)
                                        .observaciones(request.getObservaciones())
                                        .activo(true)
                                        .fechaCreacion(LocalDateTime.now())
                                        .fechaActualizacion(LocalDateTime.now())
                                        .build();

                                return repositorio.save(turno);
                            });
                });
    }

    // ── Consultas ────────────────────────────────────────────────────────────
    public Flux<TurnoDto> findAll() {
        return repositorio.findAll()
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay turnos registrados en el sistema")));
    }

    public Mono<TurnoDto> findById(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Turno no encontrado con ID: " + id)));
    }

    public Flux<TurnoDto> findByUsuario(String usuarioId) {
        return usuarioRepositorio.existsById(usuarioId)
                .flatMapMany(existe -> {
                    if (Boolean.FALSE.equals(existe)) {
                        return Flux.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + usuarioId));
                    }
                    return repositorio.findByUsuarioIdAndActivoTrue(usuarioId)
                            .switchIfEmpty(Flux.error(new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "No hay turnos activos para el usuario: " + usuarioId)));
                });
    }

    public Flux<TurnoDto> findByFecha(LocalDate fecha) {
        return repositorio.findByFechaAndActivoTrue(fecha)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay turnos activos para la fecha: " + fecha)));
    }

    public Flux<TurnoDto> findByRango(LocalDate inicio, LocalDate fin) {
        if (inicio.isAfter(fin)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser posterior a la fecha fin"));
        }
        return repositorio.findByFechaBetween(inicio, fin)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay turnos entre " + inicio + " y " + fin)));
    }

    public Flux<TurnoDto> findByUsuarioYRango(String usuarioId, LocalDate inicio, LocalDate fin) {
        if (inicio.isAfter(fin)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser posterior a la fecha fin"));
        }
        return repositorio.findByUsuarioIdAndFechaBetween(usuarioId, inicio, fin)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay turnos para el usuario en ese rango de fechas")));
    }

    // ── Actualizar ───────────────────────────────────────────────────────────
    public Mono<TurnoDto> actualizar(String id, TurnoRequest request) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Turno no encontrado con ID: " + id)))
                .flatMap(turno -> {
                    if (!turno.isActivo()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "No se puede actualizar un turno que está cancelado"));
                    }
                    if (request.getTipo() != null) {
                        LocalTime[] horas = horasPorTipo(request.getTipo());
                        turno.setTipo(request.getTipo());
                        turno.setHoraInicio(horas[0]);
                        turno.setHoraFin(horas[1]);
                    }
                    if (request.getFecha() != null)       turno.setFecha(request.getFecha());
                    if (request.getArea() != null)        turno.setArea(request.getArea());
                    if (request.getObservaciones() != null) turno.setObservaciones(request.getObservaciones());
                    turno.setFechaActualizacion(LocalDateTime.now());
                    return repositorio.save(turno);
                });
    }

    // ── Cancelar (soft-delete) ────────────────────────────────────────────────
    public Mono<TurnoDto> cancelar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Turno no encontrado con ID: " + id)))
                .flatMap(turno -> {
                    if (!turno.isActivo()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El turno ya se encuentra cancelado"));
                    }
                    turno.setActivo(false);
                    turno.setFechaActualizacion(LocalDateTime.now());
                    return repositorio.save(turno);
                });
    }

    // ── Eliminar permanente ───────────────────────────────────────────────────
    public Mono<Void> eliminar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Turno no encontrado con ID: " + id)))
                .flatMap(turno -> repositorio.deleteById(turno.getId()));
    }

    // ── Horas según tipo de turno ─────────────────────────────────────────────
    private LocalTime[] horasPorTipo(TurnoTipo tipo) {
        return switch (tipo) {
            case MANANA -> new LocalTime[]{ LocalTime.of(6, 0),  LocalTime.of(14, 0) };
            case TARDE  -> new LocalTime[]{ LocalTime.of(14, 0), LocalTime.of(22, 0) };
            case NOCHE  -> new LocalTime[]{ LocalTime.of(22, 0), LocalTime.of(6, 0)  };
        };
    }
}