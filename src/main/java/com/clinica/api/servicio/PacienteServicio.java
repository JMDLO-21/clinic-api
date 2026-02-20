package com.clinica.api.servicio;

import com.clinica.api.modelo.documento.PacienteDto;
import com.clinica.api.repositorio.PacienteRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PacienteServicio {

    private final PacienteRepositorio repositorio;

    public PacienteServicio(PacienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<PacienteDto> create(PacienteDto paciente) {
        return repositorio.save(paciente);
    }

    public Flux<PacienteDto> findAll() {
        return repositorio.findAll();
    }

    public Mono<PacienteDto> findById(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + id
                )));
    }

    public Mono<PacienteDto> update(String id, PacienteDto paciente) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + id
                )))
                .flatMap(existing -> {
                    paciente.setId(existing.getId());
                    return repositorio.save(paciente);
                });
    }

    public Mono<Void> delete(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + id
                )))
                .flatMap(paciente -> repositorio.deleteById(paciente.getId()));
    }
}