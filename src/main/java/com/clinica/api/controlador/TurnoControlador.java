package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.TurnoDto;
import com.clinica.api.modelo.request.TurnoRequest;
import com.clinica.api.servicio.TurnoServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoControlador {

    private final TurnoServicio servicio;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR')")
    public Mono<TurnoDto> crear(@Valid @RequestBody TurnoRequest request) {
        return servicio.crearTurno(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR')")
    public Flux<TurnoDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Mono<TurnoDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Flux<TurnoDto> findByUsuario(@PathVariable String usuarioId) {
        return servicio.findByUsuario(usuarioId);
    }

    @GetMapping("/fecha/{fecha}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Flux<TurnoDto> findByFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return servicio.findByFecha(fecha);
    }

    @GetMapping("/rango")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Flux<TurnoDto> findByRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return servicio.findByRango(inicio, fin);
    }

    @GetMapping("/usuario/{usuarioId}/rango")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Flux<TurnoDto> findByUsuarioYRango(
            @PathVariable String usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return servicio.findByUsuarioYRango(usuarioId, inicio, fin);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR')")
    public Mono<TurnoDto> actualizar(
            @PathVariable String id,
            @Valid @RequestBody TurnoRequest request) {
        return servicio.actualizar(id, request);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR')")
    public Mono<TurnoDto> cancelar(@PathVariable String id) {
        return servicio.cancelar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR')")
    public Mono<Void> eliminar(@PathVariable String id) {
        return servicio.eliminar(id);
    }
}