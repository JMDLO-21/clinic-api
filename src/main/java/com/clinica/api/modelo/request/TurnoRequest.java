package com.clinica.api.modelo.request;

import com.clinica.api.modelo.documento.enums.TurnoTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TurnoRequest {

    @NotBlank(message = "El ID del usuario es obligatorio")
    private String usuarioId;

    @NotNull(message = "El tipo de turno es obligatorio: MANANA, TARDE o NOCHE")
    private TurnoTipo tipo;

    @NotNull(message = "La fecha del turno es obligatoria (formato: YYYY-MM-DD)")
    private LocalDate fecha;

    private String area;
    private String observaciones;
}