package com.clinica.api.modelo.documento;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medicinas")
public class MedicinaDto {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nombre;            // Nombre comercial, ej: "Ibuprofeno 400mg"

    private String principioActivo;   // Ej: "Ibuprofeno"
    private String concentracion;     // Ej: "400mg"
    private String formaFarmaceutica; // Tableta, Jarabe, Inyectable, etc.
    private String via;               // Oral, IV, IM, Subcutánea, etc.
    private String descripcion;

    private int stockDisponible;
    private String unidadMedida;      // Tabletas, ml, ampollas, etc.

    private boolean activo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}