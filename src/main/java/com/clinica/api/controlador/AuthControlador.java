package com.clinica.api.controlador;

import com.clinica.api.modelo.request.LoginRequest;
import com.clinica.api.repositorio.UsuarioRepositorio;
import com.clinica.api.seguridad.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControlador {

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Mono<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El username no puede estar vacío"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La contraseña no puede estar vacía"));
        }

        return repositorio.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales inválidas")))
                .flatMap(usuario -> {

                    // ── LOGS DE DIAGNÓSTICO ──────────────────────────────────
                    System.out.println("=== DEBUG LOGIN ===");
                    System.out.println("Usuario encontrado : " + usuario.getUsername());
                    System.out.println("Activo             : " + usuario.isActivo());
                    System.out.println("Password recibido  : " + request.getPassword());
                    System.out.println("Hash en BD         : [" + usuario.getPassword() + "]");
                    System.out.println("Hash length        : " + (usuario.getPassword() != null ? usuario.getPassword().length() : "null"));
                    boolean match = passwordEncoder.matches(request.getPassword(), usuario.getPassword());
                    System.out.println("matches()          : " + match);
                    System.out.println("===================");
                    // ────────────────────────────────────────────────────────

                    if (!usuario.isActivo()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Usuario inactivo, contacta al administrador"));
                    }

                    if (!match) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
                    }

                    String token = jwtUtil.generarToken(
                            usuario.getUsername(),
                            usuario.getRol().name()
                    );

                    return Mono.just(Map.of(
                            "token",    token,
                            "tipo",     "Bearer",
                            "id",       usuario.getId(),
                            "username", usuario.getUsername(),
                            "email",    usuario.getEmail(),
                            "nombre",   usuario.getNombre(),
                            "apellido", usuario.getApellido(),
                            "rol",      usuario.getRol().name()
                    ));
                });
    }

    // ── ENDPOINT TEMPORAL para generar hash correcto ──────────────────────────
    // Llama GET http://localhost:8080/api/auth/hash?password=admin123
    // Copia el resultado y actualízalo en MongoDB
    // ELIMINA este endpoint antes de ir a producción
    @GetMapping("/hash")
    public Mono<String> generarHash(@RequestParam String password) {
        return Mono.just(passwordEncoder.encode(password));
    }
}

