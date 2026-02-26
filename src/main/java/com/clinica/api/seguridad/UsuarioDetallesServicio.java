package com.clinica.api.seguridad;

import com.clinica.api.repositorio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UsuarioDetallesServicio implements ReactiveUserDetailsService {

    private final UsuarioRepositorio repositorio;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repositorio.findByUsername(username)
                .switchIfEmpty(Mono.error(
                        new UsernameNotFoundException("Usuario no encontrado: " + username)))
                .flatMap(usuario -> {
                    if (!usuario.isActivo()) {
                        return Mono.error(
                                new UsernameNotFoundException("Usuario inactivo: " + username));
                    }
                    return Mono.just((UserDetails) new UsuarioDetalles(usuario));
                });
    }
}