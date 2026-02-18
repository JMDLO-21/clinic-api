package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.IngresoDto;
import com.clinica.api.modelo.documento.PacienteDto;
import com.clinica.api.modelo.request.IngresoRequest;
import com.clinica.api.servicio.IngresoServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoControlador {

    private final IngresoServicio ingresoServicio;

    @PostMapping
    public Mono<IngresoDto> crearIngreso(
            @RequestBody IngresoRequest request) {

        return ingresoServicio.crearIngreso(
                request.getPaciente(),
                request.getNumeroAdmision(),
                request.getHabitacion()
        );
    }

        // READ ALL
    @GetMapping
    public Flux<IngresoDto> findAll() {
        return ingresoServicio.findAll();
    }

    @GetMapping("/{id}")
    public Mono<IngresoDto> obtenerIngreso(@PathVariable String id) {
        return ingresoServicio.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public Mono<IngresoDto> actualizarIngreso(
            @PathVariable String id,
            @RequestBody IngresoDto ingreso) {

        return ingresoServicio.actualizar(id, ingreso);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> eliminarIngreso(@PathVariable String id) {
        return ingresoServicio.eliminar(id);
    }
}
