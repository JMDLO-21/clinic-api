package com.clinica.api.servicio;

import com.clinica.api.modelo.documento.MedicamentoPacienteDto;
import com.clinica.api.modelo.documento.enums.Rol;
import com.clinica.api.modelo.request.MedicamentoPacienteRequest;
import com.clinica.api.repositorio.MedicamentoPacienteRepositorio;
import com.clinica.api.repositorio.MedicinaRepositorio;
import com.clinica.api.repositorio.PacienteRepositorio;
import com.clinica.api.repositorio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MedicamentoPacienteServicio {

    private final MedicamentoPacienteRepositorio repositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final MedicinaRepositorio medicinaRepositorio;

    // ── Solo el médico puede prescribir ──────────────────────────────────────
    // medicoId viene del token JWT (SecurityContext), no del body
    public Mono<MedicamentoPacienteDto> prescribir(String medicoId, MedicamentoPacienteRequest request) {

        // 1. Verificar que quien prescribe sea MEDICO
        Mono<String> verificarMedico = usuarioRepositorio.findById(medicoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Médico no encontrado con ID: " + medicoId)))
                .flatMap(medico -> {
                    if (medico.getRol() != Rol.MEDICO) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Solo un MEDICO puede prescribir medicamentos"));
                    }
                    if (!medico.isActivo()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El médico se encuentra inactivo"));
                    }
                    return Mono.just(medico.getNombre() + " " + medico.getApellido());
                });

        // 2. Verificar paciente
        Mono<String> verificarPaciente = pacienteRepositorio.findById(request.getPacienteId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + request.getPacienteId())))
                .map(p -> p.getNombreCompleto().getNombres() + " " + p.getNombreCompleto().getApellidos());

        // 3. Verificar enfermero
        Mono<String> verificarEnfermero = usuarioRepositorio.findById(request.getEnfermeroId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Enfermero no encontrado con ID: " + request.getEnfermeroId())))
                .flatMap(enfermero -> {
                    if (enfermero.getRol() != Rol.ENFERMERO) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El ID proporcionado no corresponde a un ENFERMERO"));
                    }
                    if (!enfermero.isActivo()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El enfermero se encuentra inactivo"));
                    }
                    return Mono.just(enfermero.getNombre() + " " + enfermero.getApellido());
                });

        // 4. Verificar medicina y que tenga stock
        Mono<String> verificarMedicina = medicinaRepositorio.findById(request.getMedicinaId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Medicamento no encontrado con ID: " + request.getMedicinaId())))
                .flatMap(medicina -> {
                    if (!medicina.isActivo()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El medicamento '" + medicina.getNombre() + "' se encuentra inactivo"));
                    }
                    if (medicina.getStockDisponible() <= 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El medicamento '" + medicina.getNombre() + "' no tiene stock disponible"));
                    }
                    return Mono.just(medicina.getNombre());
                });

        // 5. Combinar todas las verificaciones y guardar
        return Mono.zip(verificarMedico, verificarPaciente, verificarEnfermero, verificarMedicina)
                .flatMap(tuple -> {
                    MedicamentoPacienteDto dto = MedicamentoPacienteDto.builder()
                            .pacienteId(request.getPacienteId())
                            .pacienteNombre(tuple.getT2())
                            .medicoId(medicoId)
                            .medicoNombre(tuple.getT1())
                            .enfermeroId(request.getEnfermeroId())
                            .enfermeroNombre(tuple.getT3())
                            .medicinaId(request.getMedicinaId())
                            .medicinaNombre(tuple.getT4())
                            .dosis(request.getDosis())
                            .frecuencia(request.getFrecuencia())
                            .horariosAdministracion(request.getHorariosAdministracion())
                            .fechaInicio(request.getFechaInicio())
                            .fechaFin(request.getFechaFin())
                            .indicaciones(request.getIndicaciones())
                            .estado("ACTIVO")
                            .fechaCreacion(LocalDateTime.now())
                            .fechaActualizacion(LocalDateTime.now())
                            .build();
                    return repositorio.save(dto);
                });
    }

    // ── Consultas ─────────────────────────────────────────────────────────────
    public Flux<MedicamentoPacienteDto> findAll() {
        return repositorio.findAll()
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay prescripciones registradas")));
    }

    public Mono<MedicamentoPacienteDto> findById(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prescripción no encontrada con ID: " + id)));
    }

    public Flux<MedicamentoPacienteDto> findByPaciente(String pacienteId) {
        return repositorio.findByPacienteId(pacienteId)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay prescripciones para el paciente: " + pacienteId)));
    }

    public Flux<MedicamentoPacienteDto> findByPacienteActivos(String pacienteId) {
        return repositorio.findByPacienteIdAndEstado(pacienteId, "ACTIVO")
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay medicamentos activos para el paciente: " + pacienteId)));
    }

    // Enfermero consulta los medicamentos que debe administrar en su turno
    public Flux<MedicamentoPacienteDto> findByEnfermeroActivos(String enfermeroId) {
        return repositorio.findByEnfermeroIdAndEstado(enfermeroId, "ACTIVO")
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay medicamentos activos asignados al enfermero: " + enfermeroId)));
    }

    public Flux<MedicamentoPacienteDto> findByMedico(String medicoId) {
        return repositorio.findByMedicoId(medicoId)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay prescripciones del médico: " + medicoId)));
    }

    // ── Cambiar estado (SUSPENDIDO / COMPLETADO) ──────────────────────────────
    public Mono<MedicamentoPacienteDto> cambiarEstado(String id, String nuevoEstado) {
        if (!nuevoEstado.equals("ACTIVO") && !nuevoEstado.equals("SUSPENDIDO") && !nuevoEstado.equals("COMPLETADO")) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado inválido. Use: ACTIVO, SUSPENDIDO o COMPLETADO"));
        }
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prescripción no encontrada con ID: " + id)))
                .flatMap(mp -> {
                    mp.setEstado(nuevoEstado);
                    mp.setFechaActualizacion(LocalDateTime.now());
                    return repositorio.save(mp);
                });
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    public Mono<Void> eliminar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prescripción no encontrada con ID: " + id)))
                .flatMap(mp -> repositorio.deleteById(mp.getId()));
    }
}