package com.clinica.api.repositorio;

import com.clinica.api.modelo.documento.TurnoDto;
import com.clinica.api.modelo.documento.enums.TurnoTipo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface TurnoRepositorio extends ReactiveMongoRepository<TurnoDto, String> {

    Flux<TurnoDto> findByUsuarioId(String usuarioId);
    Flux<TurnoDto> findByUsuarioIdAndActivoTrue(String usuarioId);
    Flux<TurnoDto> findByFechaAndActivoTrue(LocalDate fecha);
    Flux<TurnoDto> findByFechaBetween(LocalDate inicio, LocalDate fin);
    Flux<TurnoDto> findByUsuarioIdAndFechaBetween(String usuarioId, LocalDate inicio, LocalDate fin);

    // Verificar turno duplicado: mismo usuario, misma fecha, mismo tipo
    Mono<Boolean> existsByUsuarioIdAndFechaAndTipo(String usuarioId, LocalDate fecha, TurnoTipo tipo);
}