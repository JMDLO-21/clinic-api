package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.PacienteDto;
import com.clinica.api.servicio.PacienteServicio;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/patients")
public class PacienteControlador {

    private final PacienteServicio servicio;

    public PacienteControlador(PacienteServicio servicio) {
        this.servicio = servicio;
    }

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PacienteDto> create(@RequestBody PacienteDto paciente) {
        return servicio.create(paciente);
    }

    // READ ALL
    @GetMapping
    public Flux<PacienteDto> findAll() {
        return servicio.findAll();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Mono<PacienteDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Mono<PacienteDto> update(
            @PathVariable String id,
            @RequestBody PacienteDto paciente) {
        return servicio.update(id, paciente);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return servicio.delete(id);
    }
}
