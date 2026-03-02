package com.clinica.api.modelo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicinaRequest {

    @NotBlank(message = "El nombre del medicamento es obligatorio")
    private String nombre;

    @NotBlank(message = "El principio activo es obligatorio")
    private String principioActivo;

    private String concentracion;

    @NotBlank(message = "La forma farmacéutica es obligatoria (Tableta, Jarabe, Inyectable...)")
    private String formaFarmaceutica;

    @NotBlank(message = "La vía de administración es obligatoria (Oral, IV, IM...)")
    private String via;

    private String descripcion;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stockDisponible;

    private String unidadMedida;
}