package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.PacienteDto;
import com.clinica.api.servicio.PacienteServicio;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/patients")
public class PacienteControlador {

    private final PacienteServicio servicio;

    public PacienteControlador(PacienteServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PacienteDto> create(@RequestBody PacienteDto paciente) {
        return servicio.create(paciente);
    }

    @GetMapping
    public Flux<PacienteDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    public Mono<PacienteDto> findById(@PathVariable String id) {
        return servicio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + id
                )));
    }

    @PutMapping("/{id}")
    public Mono<PacienteDto> update(
            @PathVariable String id,
            @RequestBody PacienteDto paciente) {
        return servicio.update(id, paciente)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + id
                )));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return servicio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + id
                )))
                .flatMap(p -> servicio.delete(id));
    }
}