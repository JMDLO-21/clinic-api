package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.MedicamentoPacienteDto;
import com.clinica.api.modelo.request.MedicamentoPacienteRequest;
import com.clinica.api.seguridad.UsuarioAutenticado;
import com.clinica.api.servicio.MedicamentoPacienteServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/medicamentos-paciente")
@RequiredArgsConstructor
public class MedicamentoPacienteControlador {

    private final MedicamentoPacienteServicio servicio;
    private final UsuarioAutenticado usuarioAutenticado;

    // Solo MEDICO prescribe — el medicoId sale del token JWT
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MEDICO')")
    public Mono<MedicamentoPacienteDto> prescribir(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MedicamentoPacienteRequest request) {
        return usuarioAutenticado.obtenerIdDesdeUsername(userDetails.getUsername())
                .flatMap(medicoId -> servicio.prescribir(medicoId, request));
    }

    // ADMIN y MEDICO ven todas las prescripciones
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public Flux<MedicamentoPacienteDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Mono<MedicamentoPacienteDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    // Medicamentos de un paciente
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<MedicamentoPacienteDto> findByPaciente(@PathVariable String pacienteId) {
        return servicio.findByPaciente(pacienteId);
    }

    // Solo medicamentos ACTIVOS de un paciente (lo que debe administrarse ahora)
    @GetMapping("/paciente/{pacienteId}/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<MedicamentoPacienteDto> findByPacienteActivos(@PathVariable String pacienteId) {
        return servicio.findByPacienteActivos(pacienteId);
    }

    // Enfermero ve todos los medicamentos activos que debe administrar
    @GetMapping("/enfermero/{enfermeroId}/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO')")
    public Flux<MedicamentoPacienteDto> findByEnfermeroActivos(@PathVariable String enfermeroId) {
        return servicio.findByEnfermeroActivos(enfermeroId);
    }

    // Prescripciones hechas por un médico específico
    @GetMapping("/medico/{medicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public Flux<MedicamentoPacienteDto> findByMedico(@PathVariable String medicoId) {
        return servicio.findByMedico(medicoId);
    }

    // Cambiar estado: ACTIVO → SUSPENDIDO o COMPLETADO (solo MEDICO o ADMIN)
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public Mono<MedicamentoPacienteDto> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado) {
        return servicio.cambiarEstado(id, estado.toUpperCase());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> eliminar(@PathVariable String id) {
        return servicio.eliminar(id);
    }
}