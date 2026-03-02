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

    // ── Solo ADMIN asigna / modifica turnos ──────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<TurnoDto> crear(@Valid @RequestBody TurnoRequest request) {
        return servicio.crearTurno(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<TurnoDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Mono<TurnoDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    // Médicos y enfermeros consultan sus propios turnos
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<TurnoDto> findByUsuario(@PathVariable String usuarioId) {
        return servicio.findByUsuario(usuarioId);
    }

    // Turnos de un día específico: /api/turnos/fecha/2024-04-15
    @GetMapping("/fecha/{fecha}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<TurnoDto> findByFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return servicio.findByFecha(fecha);
    }

    // Rango de fechas: /api/turnos/rango?inicio=2024-04-01&fin=2024-04-30
    @GetMapping("/rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<TurnoDto> findByRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return servicio.findByRango(inicio, fin);
    }

    // Turnos de un usuario en un rango: /api/turnos/usuario/{id}/rango?inicio=...&fin=...
    @GetMapping("/usuario/{usuarioId}/rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<TurnoDto> findByUsuarioYRango(
            @PathVariable String usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return servicio.findByUsuarioYRango(usuarioId, inicio, fin);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<TurnoDto> actualizar(
            @PathVariable String id,
            @Valid @RequestBody TurnoRequest request) {
        return servicio.actualizar(id, request);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<TurnoDto> cancelar(@PathVariable String id) {
        return servicio.cancelar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> eliminar(@PathVariable String id) {
        return servicio.eliminar(id);
    }
}