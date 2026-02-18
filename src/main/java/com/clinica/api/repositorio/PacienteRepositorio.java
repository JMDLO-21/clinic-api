package com.clinica.api.repositorio;

import com.clinica.api.modelo.documento.PacienteDto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface PacienteRepositorio 
        extends ReactiveMongoRepository<PacienteDto, String> {

    Mono<PacienteDto> findByIdentificacionNumero(String numero);
}
