package com.clinica.api.controlador;

import com.clinica.api.modelo.documento.MedicamentoPacienteDto;
import com.clinica.api.modelo.request.AdministrarMedicamentoRequest;
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

    // Solo MEDICO prescribe
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MEDICO')")
    public Mono<MedicamentoPacienteDto> prescribir(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MedicamentoPacienteRequest request) {
        return usuarioAutenticado.obtenerIdDesdeUsername(userDetails.getUsername())
                .flatMap(medicoId -> servicio.prescribir(medicoId, request));
    }

    // ENFERMERO marca que ya administró una toma específica
    // PATCH /api/medicamentos-paciente/{id}/administrar
    @PatchMapping("/{id}/administrar")
    @PreAuthorize("hasRole('ENFERMERO')")
    public Mono<MedicamentoPacienteDto> administrar(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AdministrarMedicamentoRequest request) {
        return usuarioAutenticado.obtenerIdDesdeUsername(userDetails.getUsername())
                .flatMap(enfermeroId -> servicio.administrar(id, enfermeroId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'COORDINADOR')")
    public Flux<MedicamentoPacienteDto> findAll() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'COORDINADOR')")
    public Mono<MedicamentoPacienteDto> findById(@PathVariable String id) {
        return servicio.findById(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'COORDINADOR')")
    public Flux<MedicamentoPacienteDto> findByPaciente(@PathVariable String pacienteId) {
        return servicio.findByPaciente(pacienteId);
    }

    @GetMapping("/paciente/{pacienteId}/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'COORDINADOR')")
    public Flux<MedicamentoPacienteDto> findByPacienteActivos(
            @PathVariable String pacienteId) {
        return servicio.findByPacienteActivos(pacienteId);
    }

    // Tomas que aún no se han administrado de un paciente
    @GetMapping("/paciente/{pacienteId}/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'ENFERMERO', 'COORDINADOR')")
    public Flux<MedicamentoPacienteDto> findPendientesPorPaciente(
            @PathVariable String pacienteId) {
        return servicio.findPendientesPorPaciente(pacienteId);
    }

    @GetMapping("/medico/{medicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'COORDINADOR')")
    public Flux<MedicamentoPacienteDto> findByMedico(@PathVariable String medicoId) {
        return servicio.findByMedico(medicoId);
    }

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