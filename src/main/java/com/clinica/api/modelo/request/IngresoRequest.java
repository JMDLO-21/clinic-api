package com.clinica.api.modelo.request;

import com.clinica.api.modelo.documento.PacienteDto;
import lombok.Data;

@Data
public class IngresoRequest {

    private PacienteDto paciente;

    private String numeroAdmision;

    private String habitacion;
}
