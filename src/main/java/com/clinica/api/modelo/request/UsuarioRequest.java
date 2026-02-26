package com.clinica.api.modelo.request;

import com.clinica.api.modelo.documento.enums.Rol;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotBlank(message = "La cédula es obligatoria")
    private String cedula;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    private String telefono;

    @NotNull(message = "El rol es obligatorio: MEDICO, ENFERMERO o ADMIN")
    private Rol rol;

    // ── Campos MEDICO ──
    private String especialidad;
    private String registroMedico;

    // ── Campos ENFERMERO ──
    private String registroEnfermero;
    private String areaAsignada;
}