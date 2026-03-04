package com.clinica.api.modelo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdministrarMedicamentoRequest {

    @NotBlank(message = "El horario es obligatorio (ej: '08:00')")
    private String horario; // qué toma está marcando, ej: "08:00"

    private String observaciones; // opcional, nota del enfermero
}