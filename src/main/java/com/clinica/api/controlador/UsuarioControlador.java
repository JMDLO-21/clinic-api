package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.UsuarioDto;
import com.clinica.api.modelo.documento.enums.Rol;
import com.clinica.api.modelo.request.UsuarioRequest;
import com.clinica.api.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioControlador {

    private final UsuarioServicio servicio;

    // ── Solo ADMIN puede crear usuarios ──────────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UsuarioDto> crear(@Valid @RequestBody UsuarioRequest request) {
        return servicio.crearUsuario(request);
    }

    // ── ADMIN ve todos; MEDICO y ENFERMERO pueden listar para conocer colegas ─
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<UsuarioDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Mono<UsuarioDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    // Listar por rol: ej. /api/usuarios/rol/ENFERMERO  o  /api/usuarios/rol/MEDICO
    @GetMapping("/rol/{rol}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<UsuarioDto> findByRol(@PathVariable Rol rol) {
        return servicio.findByRol(rol);
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<UsuarioDto> findActivos() {
        return servicio.findActivos();
    }

    // ── Solo ADMIN puede actualizar / desactivar / eliminar ──────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UsuarioDto> actualizar(
            @PathVariable String id,
            @RequestBody UsuarioRequest request) {
        return servicio.actualizar(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UsuarioDto> desactivar(@PathVariable String id) {
        return servicio.desactivar(id);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UsuarioDto> activar(@PathVariable String id) {
        return servicio.activar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> eliminar(@PathVariable String id) {
        return servicio.eliminar(id);
    }
}