package com.clinica.api.modelo.documento;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ingresos")
public class IngresoDto {

    @Id
    private String id;

    private String pacienteId;

    private String numeroAdmision;

    private String habitacion;

    private LocalDateTime fechaIngreso;

    private String estado; // ACTIVO, ALTA
}
