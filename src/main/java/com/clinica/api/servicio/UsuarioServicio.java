package com.clinica.api.servicio;

import com.clinica.api.modelo.documento.UsuarioDto;
import com.clinica.api.modelo.documento.enums.Rol;
import com.clinica.api.modelo.request.UsuarioRequest;
import com.clinica.api.repositorio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioServicio {

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder passwordEncoder;

    // ── Crear usuario con rol definido ──────────────────────────────────────
    public Mono<UsuarioDto> crearUsuario(UsuarioRequest request) {

        // Validaciones de negocio según el rol
        if (request.getRol() == Rol.MEDICO) {
            if (esBlanco(request.getEspecialidad())) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El campo 'especialidad' es obligatorio para el rol MEDICO"));
            }
            if (esBlanco(request.getRegistroMedico())) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El campo 'registroMedico' es obligatorio para el rol MEDICO"));
            }
        }

        if (request.getRol() == Rol.ENFERMERO) {
            if (esBlanco(request.getRegistroEnfermero())) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El campo 'registroEnfermero' es obligatorio para el rol ENFERMERO"));
            }
        }

        // Verificar duplicados en paralelo
        return Mono.zip(
                repositorio.existsByUsername(request.getUsername()),
                repositorio.existsByEmail(request.getEmail()),
                repositorio.existsByCedula(request.getCedula())
        ).flatMap(tuple -> {
            if (Boolean.TRUE.equals(tuple.getT1())) {
                return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con el username: " + request.getUsername()));
            }
            if (Boolean.TRUE.equals(tuple.getT2())) {
                return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con el email: " + request.getEmail()));
            }
            if (Boolean.TRUE.equals(tuple.getT3())) {
                return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con la cédula: " + request.getCedula()));
            }

            UsuarioDto usuario = UsuarioDto.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .cedula(request.getCedula())
                    .nombre(request.getNombre())
                    .apellido(request.getApellido())
                    .telefono(request.getTelefono())
                    .rol(request.getRol())
                    .especialidad(request.getEspecialidad())
                    .registroMedico(request.getRegistroMedico())
                    .registroEnfermero(request.getRegistroEnfermero())
                    .areaAsignada(request.getAreaAsignada())
                    .activo(true)
                    .fechaCreacion(LocalDateTime.now())
                    .fechaActualizacion(LocalDateTime.now())
                    .build();

            return repositorio.save(usuario);
        });
    }

    // ── Consultas ────────────────────────────────────────────────────────────
    public Flux<UsuarioDto> findAll() {
        return repositorio.findAll()
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay usuarios registrados en el sistema")));
    }

    public Mono<UsuarioDto> findById(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id)));
    }

    public Flux<UsuarioDto> findByRol(Rol rol) {
        return repositorio.findByRol(rol)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay usuarios con el rol: " + rol)));
    }

    public Flux<UsuarioDto> findActivos() {
        return repositorio.findByActivoTrue()
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay usuarios activos en el sistema")));
    }

    // ── Actualizar ──────────────────────────────────────────────────────────
    public Mono<UsuarioDto> actualizar(String id, UsuarioRequest request) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id)))
                .flatMap(existing -> {
                    if (request.getNombre() != null)    existing.setNombre(request.getNombre());
                    if (request.getApellido() != null)  existing.setApellido(request.getApellido());
                    if (request.getTelefono() != null)  existing.setTelefono(request.getTelefono());
                    if (request.getEspecialidad() != null)    existing.setEspecialidad(request.getEspecialidad());
                    if (request.getRegistroMedico() != null)  existing.setRegistroMedico(request.getRegistroMedico());
                    if (request.getRegistroEnfermero() != null) existing.setRegistroEnfermero(request.getRegistroEnfermero());
                    if (request.getAreaAsignada() != null)    existing.setAreaAsignada(request.getAreaAsignada());
                    if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        existing.setPassword(passwordEncoder.encode(request.getPassword()));
                    }
                    existing.setFechaActualizacion(LocalDateTime.now());
                    return repositorio.save(existing);
                });
    }

    // ── Activar / Desactivar ────────────────────────────────────────────────
    public Mono<UsuarioDto> desactivar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id)))
                .flatMap(usuario -> {
                    if (!usuario.isActivo()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "El usuario ya se encuentra inactivo"));
                    }
                    usuario.setActivo(false);
                    usuario.setFechaActualizacion(LocalDateTime.now());
                    return repositorio.save(usuario);
                });
    }

    public Mono<UsuarioDto> activar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id)))
                .flatMap(usuario -> {
                    if (usuario.isActivo()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "El usuario ya se encuentra activo"));
                    }
                    usuario.setActivo(true);
                    usuario.setFechaActualizacion(LocalDateTime.now());
                    return repositorio.save(usuario);
                });
    }

    // ── Eliminar ─────────────────────────────────────────────────────────────
    public Mono<Void> eliminar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id)))
                .flatMap(usuario -> repositorio.deleteById(usuario.getId()));
    }

    // ── Para Spring Security ─────────────────────────────────────────────────
    public Mono<UsuarioDto> findByUsername(String username) {
        return repositorio.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con username: " + username)));
    }

    // ── Utilidad ─────────────────────────────────────────────────────────────
    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}