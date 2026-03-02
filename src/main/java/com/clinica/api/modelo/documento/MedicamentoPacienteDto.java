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

    // Médico que prescribió el tratamiento
    private String medicoId;
    private String medicoNombre;

    // Enfermero responsable de administrar
    private String enfermeroId;
    private String enfermeroNombre;

    // Medicina del catálogo
    private String medicinaId;
    private String medicinaNombre;

    // Detalles del tratamiento
    private String dosis;                        // Ej: "1 tableta", "500mg"
    private String frecuencia;                   // Ej: "Cada 8 horas"
    private List<String> horariosAdministracion; // Ej: ["08:00", "16:00", "00:00"]

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private String indicaciones;  // Notas adicionales del médico
    private String estado;        // ACTIVO | SUSPENDIDO | COMPLETADO

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}