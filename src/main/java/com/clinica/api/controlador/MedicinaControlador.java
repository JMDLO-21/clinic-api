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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<MedicinaDto> crear(@Valid @RequestBody MedicinaRequest request) {
        return servicio.crear(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Flux<MedicinaDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Mono<MedicinaDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Flux<MedicinaDto> findActivos() {
        return servicio.findActivos();
    }

    @GetMapping("/con-stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Flux<MedicinaDto> findConStock() {
        return servicio.findConStock();
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COORDINADOR', 'ROLE_MEDICO', 'ROLE_ENFERMERO')")
    public Flux<MedicinaDto> buscarPorPrincipioActivo(@RequestParam String principioActivo) {
        return servicio.buscarPorPrincipioActivo(principioActivo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<MedicinaDto> actualizar(
            @PathVariable String id,
            @Valid @RequestBody MedicinaRequest request) {
        return servicio.actualizar(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<MedicinaDto> desactivar(@PathVariable String id) {
        return servicio.desactivar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<Void> eliminar(@PathVariable String id) {
        return servicio.eliminar(id);
    }
}