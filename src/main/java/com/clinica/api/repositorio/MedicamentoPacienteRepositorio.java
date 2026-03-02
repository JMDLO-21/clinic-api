package com.clinica.api.repositorio;

import com.clinica.api.modelo.documento.MedicamentoPacienteDto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MedicamentoPacienteRepositorio extends ReactiveMongoRepository<MedicamentoPacienteDto, String> {

    Flux<MedicamentoPacienteDto> findByPacienteId(String pacienteId);
    Flux<MedicamentoPacienteDto> findByEnfermeroId(String enfermeroId);
    Flux<MedicamentoPacienteDto> findByMedicoId(String medicoId);
    Flux<MedicamentoPacienteDto> findByPacienteIdAndEstado(String pacienteId, String estado);
    Flux<MedicamentoPacienteDto> findByEnfermeroIdAndEstado(String enfermeroId, String estado);
}