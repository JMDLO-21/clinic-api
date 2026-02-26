package com.clinica.api.repositorio;

import com.clinica.api.modelo.documento.UsuarioDto;
import com.clinica.api.modelo.documento.enums.Rol;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsuarioRepositorio extends ReactiveMongoRepository<UsuarioDto, String> {

    Mono<UsuarioDto> findByUsername(String username);
    Mono<UsuarioDto> findByEmail(String email);
    Mono<UsuarioDto> findByCedula(String cedula);

    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByCedula(String cedula);

    Flux<UsuarioDto> findByRol(Rol rol);
    Flux<UsuarioDto> findByRolAndActivoTrue(Rol rol);
    Flux<UsuarioDto> findByActivoTrue();
}