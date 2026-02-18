package com.clinica.api.servicio;

import com.clinica.api.modelo.documento.IngresoDto;
import com.clinica.api.modelo.documento.PacienteDto;
import com.clinica.api.repositorio.IngresoRepositorio;
import com.clinica.api.repositorio.PacienteRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IngresoServicio {

    private final IngresoRepositorio ingresoRepository;
    private final PacienteRepositorio pacienteRepository;

    public Mono<IngresoDto> crearIngreso(PacienteDto paciente,
                                          String numeroAdmision,
                                          String habitacion) {

        return pacienteRepository
                .findByIdentificacionNumero(
                        paciente.getIdentificacion().getNumero()
                )
                .switchIfEmpty(pacienteRepository.save(paciente))
                .flatMap(pacienteGuardado -> {

                    IngresoDto ingreso = IngresoDto.builder()
                            .pacienteId(pacienteGuardado.getId())
                            .numeroAdmision(numeroAdmision)
                            .habitacion(habitacion)
                            .fechaIngreso(LocalDateTime.now())
                            .estado("ACTIVO")
                            .build();

                    return ingresoRepository.save(ingreso);
                });
    }

        public Flux<IngresoDto> findAll() {
        return ingresoRepository.findAll();
    }

    public Mono<IngresoDto> obtenerPorId(String id) {
        return ingresoRepository.findById(id);
    }

    public Mono<Void> eliminar(String id) {
        return ingresoRepository.deleteById(id);
    }

    public Mono<IngresoDto> actualizar(String id, IngresoDto ingresoActualizado) {

        return ingresoRepository.findById(id)
                .flatMap(existing -> {
                    existing.setHabitacion(ingresoActualizado.getHabitacion());
                    existing.setEstado(ingresoActualizado.getEstado());
                    return ingresoRepository.save(existing);
                });
    }
}
