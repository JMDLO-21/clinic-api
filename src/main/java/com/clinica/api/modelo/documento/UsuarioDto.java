package com.clinica.api.modelo.documento;

import com.clinica.api.modelo.documento.enums.Rol;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuarios")
public class UsuarioDto {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String cedula;

    private String nombre;
    private String apellido;
    private String telefono;

    private Rol rol;

    // Solo aplica cuando rol = MEDICO
    private String especialidad;
    private String registroMedico;

    // Solo aplica cuando rol = ENFERMERO
    private String registroEnfermero;
    private String areaAsignada;

    private boolean activo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}