package com.clinica.api.modelo.documento;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "patients")
public class PacienteDto {

    @Id
    private String id;

    private Identificacion identificacion;
    private NombreCompleto nombreCompleto;

    private LocalDate fechaNacimiento;
    private String sexo;

    private Contacto contacto;
    private EPS eps;

    private HistorialClinicoBase historialClinicoBase;

    // ===== CONSTRUCTORES =====

    public PacienteDto() {
    }

    // ===== GETTERS Y SETTERS =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Identificacion getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(Identificacion identificacion) {
        this.identificacion = identificacion;
    }

    public NombreCompleto getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(NombreCompleto nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Contacto getContacto() {
        return contacto;
    }

    public void setContacto(Contacto contacto) {
        this.contacto = contacto;
    }

    public EPS getEps() {
        return eps;
    }

    public void setEps(EPS eps) {
        this.eps = eps;
    }

    public HistorialClinicoBase getHistorialClinicoBase() {
        return historialClinicoBase;
    }

    public void setHistorialClinicoBase(HistorialClinicoBase historialClinicoBase) {
        this.historialClinicoBase = historialClinicoBase;
    }

    // ===== SUBDOCUMENTOS =====

    public static class Identificacion {
        private String tipo;
        private String numero;

        public Identificacion() {}

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public String getNumero() { return numero; }
        public void setNumero(String numero) { this.numero = numero; }
    }

    public static class NombreCompleto {
        private String nombres;
        private String apellidos;

        public NombreCompleto() {}

        public String getNombres() { return nombres; }
        public void setNombres(String nombres) { this.nombres = nombres; }

        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    }

    public static class Contacto {
        private String telefono;
        private String direccion;

        public Contacto() {}

        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }

        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
    }

    public static class EPS {
        private String nombre;
        private String tipoAfiliacion;

        public EPS() {}

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getTipoAfiliacion() { return tipoAfiliacion; }
        public void setTipoAfiliacion(String tipoAfiliacion) { this.tipoAfiliacion = tipoAfiliacion; }
    }

    public static class HistorialClinicoBase {
        private List<String> alergias;
        private List<String> enfermedadesCronicas;
        private List<String> antecedentesQuirurgicos;

        public HistorialClinicoBase() {}

        public List<String> getAlergias() { return alergias; }
        public void setAlergias(List<String> alergias) { this.alergias = alergias; }

        public List<String> getEnfermedadesCronicas() { return enfermedadesCronicas; }
        public void setEnfermedadesCronicas(List<String> enfermedadesCronicas) { this.enfermedadesCronicas = enfermedadesCronicas; }

        public List<String> getAntecedentesQuirurgicos() { return antecedentesQuirurgicos; }
        public void setAntecedentesQuirurgicos(List<String> antecedentesQuirurgicos) { this.antecedentesQuirurgicos = antecedentesQuirurgicos; }
    }
}
