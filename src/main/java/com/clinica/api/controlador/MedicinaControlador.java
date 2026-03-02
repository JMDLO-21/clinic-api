package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.MedicinaDto;
import com.clinica.api.modelo.request.MedicinaRequest;
import com.clinica.api.servicio.MedicinaServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/medicinas")
@RequiredArgsConstructor
public class MedicinaControlador {

    private final MedicinaServicio servicio;

    // ADMIN gestiona el catálogo de medicamentos
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<MedicinaDto> crear(@Valid @RequestBody MedicinaRequest request) {
        return servicio.crear(request);
    }

    // MEDICO y ENFERMERO pueden ver el catálogo disponible
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<MedicinaDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<MedicinaDto> findActivos() {
        return servicio.findActivos();
    }

    @GetMapping("/con-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<MedicinaDto> findConStock() {
        return servicio.findConStock();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Mono<MedicinaDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    // Buscar por principio activo: /api/medicinas/buscar?principio=ibuprofeno
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<MedicinaDto> buscarPorPrincipioActivo(@RequestParam String principio) {
        return servicio.buscarPorPrincipioActivo(principio);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<MedicinaDto> actualizar(
            @PathVariable String id,
            @Valid @RequestBody MedicinaRequest request) {
        return servicio.actualizar(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<MedicinaDto> desactivar(@PathVariable String id) {
        return servicio.desactivar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> eliminar(@PathVariable String id) {
        return servicio.eliminar(id);
    }
}