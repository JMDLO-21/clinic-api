package com.clinica.api.repositorio;

import com.clinica.api.modelo.documento.MedicinaDto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MedicinaRepositorio extends ReactiveMongoRepository<MedicinaDto, String> {

    Mono<MedicinaDto> findByNombreIgnoreCase(String nombre);
    Mono<Boolean> existsByNombreIgnoreCase(String nombre);

    Flux<MedicinaDto> findByActivoTrue();
    Flux<MedicinaDto> findByPrincipioActivoIgnoreCaseContaining(String principioActivo);
    Flux<MedicinaDto> findByStockDisponibleGreaterThan(int cantidad);
}