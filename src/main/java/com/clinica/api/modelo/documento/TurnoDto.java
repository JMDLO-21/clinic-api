package com.clinica.api.modelo.documento;

import com.clinica.api.modelo.documento.enums.TurnoTipo;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "turnos")
public class TurnoDto {

    @Id
    private String id;

    private String usuarioId;       // ID del médico o enfermero
    private String usuarioNombre;   // Nombre completo para referencia rápida

    private TurnoTipo tipo;         // MANANA | TARDE | NOCHE

    private LocalDate fecha;
    private LocalTime horaInicio;   // Se asigna automáticamente según el tipo
    private LocalTime horaFin;

    private String area;
    private String observaciones;

    private boolean activo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}