package com.clinica.api.repositorio;

import com.clinica.api.modelo.documento.IngresoDto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface IngresoRepositorio extends ReactiveMongoRepository<IngresoDto, String> {

    Flux<IngresoDto> findByPacienteId(String pacienteId);
}
