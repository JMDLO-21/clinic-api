package com.clinica.api.seguridad;

import com.clinica.api.repositorio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UsuarioAutenticado {

    private final UsuarioRepositorio repositorio;

    /**
     * Dado el username del token JWT, retorna el ID de MongoDB del usuario.
     * Usado en controladores para saber quién está haciendo la acción.
     */
    public Mono<String> obtenerIdDesdeUsername(String username) {
        return repositorio.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario autenticado no encontrado: " + username)))
                .map(usuario -> usuario.getId());
    }
}