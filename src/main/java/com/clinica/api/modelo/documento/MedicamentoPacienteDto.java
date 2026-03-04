package com.clinica.api.modelo.documento;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medicamentos_paciente")
public class MedicamentoPacienteDto {

    @Id
    private String id;

    // Paciente
    private String pacienteId;
    private String pacienteNombre;

    // Médico que prescribió
    private String medicoId;
    private String medicoNombre;

    // Medicina del catálogo
    private String medicinaId;
    private String medicinaNombre;

    // Detalles del tratamiento
    private String dosis;
    private String frecuencia;

    // Lista de administraciones programadas
    // Cada una tiene su horario y su estado individual
    private List<AdministracionDto> administraciones;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private String indicaciones;
    private String estado; // ACTIVO | SUSPENDIDO | COMPLETADO

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // ── Subdocumento: cada toma programada ──────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdministracionDto {

        private String horario;          // "08:00", "16:00", "00:00"

        // Se llena cuando el enfermero marca que ya lo colocó
        private boolean administrado;
        private String enfermeroId;      // quién lo colocó
        private String enfermeroNombre;
        private LocalDateTime fechaAdministracion; // cuándo exactamente lo colocó
        private String observaciones;    // nota opcional del enfermero
    }
}