package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.UsuarioDto;
import com.clinica.api.modelo.documento.enums.Rol;
import com.clinica.api.modelo.request.UsuarioRequest;
import com.clinica.api.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioControlador {

    private final UsuarioServicio servicio;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public Mono<UsuarioDto> crear(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UsuarioRequest request) {

        // Si quien crea es COORDINADOR, solo puede crear MEDICO y ENFERMERO
        boolean esCoordinador = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COORDINADOR"));

        if (esCoordinador) {
            if (request.getRol() == Rol.ADMIN || request.getRol() == Rol.COORDINADOR) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un COORDINADOR solo puede crear usuarios MEDICO o ENFERMERO"));
            }
        }

        return servicio.crearUsuario(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR', 'MEDICO', 'ENFERMERO')")
    public Flux<UsuarioDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR', 'MEDICO', 'ENFERMERO')")
    public Mono<UsuarioDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    @GetMapping("/rol/{rol}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR', 'MEDICO', 'ENFERMERO')")
    public Flux<UsuarioDto> findByRol(@PathVariable Rol rol) {
        return servicio.findByRol(rol);
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR', 'MEDICO', 'ENFERMERO')")
    public Flux<UsuarioDto> findActivos() {
        return servicio.findActivos();
    }

    // Solo ADMIN puede actualizar, desactivar y eliminar cualquier usuario
    // COORDINADOR puede actualizar solo MEDICO y ENFERMERO
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public Mono<UsuarioDto> actualizar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody UsuarioRequest request) {

        boolean esCoordinador = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COORDINADOR"));

        if (esCoordinador) {
            // Verificar que el usuario a modificar no sea ADMIN ni COORDINADOR
            return servicio.findById(id)
                    .flatMap(usuario -> {
                        if (usuario.getRol() == Rol.ADMIN
                                || usuario.getRol() == Rol.COORDINADOR) {
                            return Mono.error(new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Un COORDINADOR no puede modificar ADMIN ni COORDINADOR"));
                        }
                        return servicio.actualizar(id, request);
                    });
        }

        return servicio.actualizar(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public Mono<UsuarioDto> desactivar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        boolean esCoordinador = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COORDINADOR"));

        if (esCoordinador) {
            return servicio.findById(id)
                    .flatMap(usuario -> {
                        if (usuario.getRol() == Rol.ADMIN
                                || usuario.getRol() == Rol.COORDINADOR) {
                            return Mono.error(new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Un COORDINADOR no puede desactivar ADMIN ni COORDINADOR"));
                        }
                        return servicio.desactivar(id);
                    });
        }

        return servicio.desactivar(id);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public Mono<UsuarioDto> activar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        boolean esCoordinador = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COORDINADOR"));

        if (esCoordinador) {
            return servicio.findById(id)
                    .flatMap(usuario -> {
                        if (usuario.getRol() == Rol.ADMIN
                                || usuario.getRol() == Rol.COORDINADOR) {
                            return Mono.error(new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Un COORDINADOR no puede activar ADMIN ni COORDINADOR"));
                        }
                        return servicio.activar(id);
                    });
        }

        return servicio.activar(id);
    }

    // Solo ADMIN puede eliminar usuarios
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> eliminar(@PathVariable String id) {
        return servicio.eliminar(id);
    }
}