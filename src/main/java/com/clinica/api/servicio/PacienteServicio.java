package com.clinica.api.servicio;

import com.clinica.api.modelo.documento.PacienteDto;
import com.clinica.api.repositorio.PacienteRepositorio;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PacienteServicio {

    private final PacienteRepositorio repositorio;

    public PacienteServicio(PacienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // CREATE
    public Mono<PacienteDto> create(PacienteDto paciente) {
        return repositorio.save(paciente);
    }

    // READ ALL
    public Flux<PacienteDto> findAll() {
        return repositorio.findAll();
    }

    // READ BY ID
    public Mono<PacienteDto> findById(String id) {
        return repositorio.findById(id);
    }

    // UPDATE
    public Mono<PacienteDto> update(String id, PacienteDto paciente) {
        return repositorio.findById(id)
                .flatMap(existing -> {
                    paciente.setId(existing.getId());
                    return repositorio.save(paciente);
                });
    }

    // DELETE
    public Mono<Void> delete(String id) {
        return repositorio.deleteById(id);
    }
}
