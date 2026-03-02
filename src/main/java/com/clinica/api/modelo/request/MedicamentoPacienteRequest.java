package com.clinica.api.modelo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicamentoPacienteRequest {

    @NotBlank(message = "El ID del paciente es obligatorio")
    private String pacienteId;

    @NotBlank(message = "El ID del enfermero responsable es obligatorio")
    private String enfermeroId;

    @NotBlank(message = "El ID del medicamento es obligatorio")
    private String medicinaId;

    @NotBlank(message = "La dosis es obligatoria (ej: '1 tableta', '500mg')")
    private String dosis;

    @NotBlank(message = "La frecuencia es obligatoria (ej: 'Cada 8 horas')")
    private String frecuencia;

    @NotEmpty(message = "Debe indicar al menos un horario de administración (ej: ['08:00','16:00'])")
    private List<String> horariosAdministracion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private String indicaciones;
}