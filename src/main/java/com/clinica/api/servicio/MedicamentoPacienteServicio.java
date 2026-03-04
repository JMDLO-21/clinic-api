package com.clinica.api.servicio;

import com.clinica.api.modelo.documento.MedicamentoPacienteDto;
import com.clinica.api.modelo.documento.MedicamentoPacienteDto.AdministracionDto;
import com.clinica.api.modelo.documento.enums.Rol;
import com.clinica.api.modelo.request.AdministrarMedicamentoRequest;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicamentoPacienteServicio {

    private final MedicamentoPacienteRepositorio repositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final MedicinaRepositorio medicinaRepositorio;

    // ── MEDICO prescribe ─────────────────────────────────────────────────────
    public Mono<MedicamentoPacienteDto> prescribir(
            String medicoId,
            MedicamentoPacienteRequest request) {

        // Verificar médico
        Mono<String> verificarMedico = usuarioRepositorio.findById(medicoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Médico no encontrado")))
                .flatMap(medico -> {
                    if (medico.getRol() != Rol.MEDICO) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Solo un MEDICO puede prescribir"));
                    }
                    if (!medico.isActivo()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "El médico está inactivo"));
                    }
                    return Mono.just(medico.getNombre() + " " + medico.getApellido());
                });

        // Verificar paciente
        Mono<String> verificarPaciente = pacienteRepositorio
                .findById(request.getPacienteId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paciente no encontrado con ID: " + request.getPacienteId())))
                .map(p -> p.getNombreCompleto().getNombres()
                        + " " + p.getNombreCompleto().getApellidos());

        // Verificar medicina con stock
        Mono<String> verificarMedicina = medicinaRepositorio
                .findById(request.getMedicinaId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Medicamento no encontrado con ID: " + request.getMedicinaId())))
                .flatMap(medicina -> {
                    if (!medicina.isActivo()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "El medicamento '" + medicina.getNombre() + "' está inactivo"));
                    }
                    if (medicina.getStockDisponible() <= 0) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "El medicamento '" + medicina.getNombre()
                                        + "' no tiene stock disponible"));
                    }
                    return Mono.just(medicina.getNombre());
                });

        return Mono.zip(verificarMedico, verificarPaciente, verificarMedicina)
                .flatMap(tuple -> {

                    // Construir la lista de administraciones — una por cada horario
                    // Todas empiezan como no administradas
                    List<AdministracionDto> administraciones = request.getHorarios()
                            .stream()
                            .map(horario -> AdministracionDto.builder()
                                    .horario(horario)
                                    .administrado(false)
                                    .build())
                            .collect(Collectors.toList());

                    MedicamentoPacienteDto dto = MedicamentoPacienteDto.builder()
                            .pacienteId(request.getPacienteId())
                            .pacienteNombre(tuple.getT2())
                            .medicoId(medicoId)
                            .medicoNombre(tuple.getT1())
                            .medicinaId(request.getMedicinaId())
                            .medicinaNombre(tuple.getT3())
                            .dosis(request.getDosis())
                            .frecuencia(request.getFrecuencia())
                            .administraciones(administraciones)
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

    // ── ENFERMERO marca que ya administró una toma ────────────────────────────
    public Mono<MedicamentoPacienteDto> administrar(
            String medicamentoPacienteId,
            String enfermeroId,
            AdministrarMedicamentoRequest request) {

        // Verificar que quien administra es ENFERMERO
        Mono<String[]> verificarEnfermero = usuarioRepositorio.findById(enfermeroId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Enfermero no encontrado")))
                .flatMap(enfermero -> {
                    if (enfermero.getRol() != Rol.ENFERMERO) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Solo un ENFERMERO puede registrar la administración"));
                    }
                    if (!enfermero.isActivo()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "El enfermero está inactivo"));
                    }
                    return Mono.just(new String[]{
                            enfermero.getId(),
                            enfermero.getNombre() + " " + enfermero.getApellido()
                    });
                });

        return verificarEnfermero.flatMap(datosEnfermero ->
                repositorio.findById(medicamentoPacienteId)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Prescripción no encontrada con ID: " + medicamentoPacienteId)))
                        .flatMap(mp -> {

                            if (!mp.getEstado().equals("ACTIVO")) {
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "No se puede administrar una prescripción "
                                                + mp.getEstado()));
                            }

                            // Buscar el horario específico que el enfermero está marcando
                            boolean horarioEncontrado = false;
                            boolean yaAdministrado = false;

                            for (AdministracionDto adm : mp.getAdministraciones()) {
                                if (adm.getHorario().equals(request.getHorario())) {
                                    horarioEncontrado = true;
                                    if (adm.isAdministrado()) {
                                        yaAdministrado = true;
                                    }
                                    break;
                                }
                            }

                            if (!horarioEncontrado) {
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "El horario '" + request.getHorario()
                                                + "' no existe en esta prescripción"));
                            }

                            if (yaAdministrado) {
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "El medicamento del horario '"
                                                + request.getHorario()
                                                + "' ya fue administrado"));
                            }

                            // Marcar esa toma específica como administrada
                            mp.getAdministraciones().forEach(adm -> {
                                if (adm.getHorario().equals(request.getHorario())) {
                                    adm.setAdministrado(true);
                                    adm.setEnfermeroId(datosEnfermero[0]);
                                    adm.setEnfermeroNombre(datosEnfermero[1]);
                                    adm.setFechaAdministracion(LocalDateTime.now());
                                    adm.setObservaciones(request.getObservaciones());
                                }
                            });

                            // Si TODAS las tomas fueron administradas
                            // marcar la prescripción completa como COMPLETADO
                            boolean todasAdministradas = mp.getAdministraciones()
                                    .stream()
                                    .allMatch(AdministracionDto::isAdministrado);

                            if (todasAdministradas) {
                                mp.setEstado("COMPLETADO");
                            }

                            mp.setFechaActualizacion(LocalDateTime.now());
                            return repositorio.save(mp);
                        })
        );
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
                        HttpStatus.NOT_FOUND,
                        "No hay prescripciones para el paciente: " + pacienteId)));
    }

    public Flux<MedicamentoPacienteDto> findByPacienteActivos(String pacienteId) {
        return repositorio.findByPacienteIdAndEstado(pacienteId, "ACTIVO")
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay medicamentos activos para el paciente: " + pacienteId)));
    }

    public Flux<MedicamentoPacienteDto> findByMedico(String medicoId) {
        return repositorio.findByMedicoId(medicoId)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay prescripciones del médico: " + medicoId)));
    }

    // Pendientes = tomas que aún no se han administrado
    public Flux<MedicamentoPacienteDto> findPendientesPorPaciente(String pacienteId) {
        return repositorio.findByPacienteIdAndEstado(pacienteId, "ACTIVO")
                .filter(mp -> mp.getAdministraciones().stream()
                        .anyMatch(adm -> !adm.isAdministrado()))
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay tomas pendientes para el paciente: " + pacienteId)));
    }

    public Mono<MedicamentoPacienteDto> cambiarEstado(String id, String nuevoEstado) {
        if (!nuevoEstado.equals("ACTIVO")
                && !nuevoEstado.equals("SUSPENDIDO")
                && !nuevoEstado.equals("COMPLETADO")) {
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

    public Mono<Void> eliminar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prescripción no encontrada con ID: " + id)))
                .flatMap(mp -> repositorio.deleteById(mp.getId()));
    }
}