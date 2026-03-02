package com.clinica.api.servicio;

import com.clinica.api.modelo.documento.MedicinaDto;
import com.clinica.api.modelo.request.MedicinaRequest;
import com.clinica.api.repositorio.MedicinaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MedicinaServicio {

    private final MedicinaRepositorio repositorio;

    // ── Crear ─────────────────────────────────────────────────────────────────
    public Mono<MedicinaDto> crear(MedicinaRequest request) {
        return repositorio.existsByNombreIgnoreCase(request.getNombre())
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                                "Ya existe un medicamento con el nombre: " + request.getNombre()));
                    }
                    MedicinaDto medicina = MedicinaDto.builder()
                            .nombre(request.getNombre())
                            .principioActivo(request.getPrincipioActivo())
                            .concentracion(request.getConcentracion())
                            .formaFarmaceutica(request.getFormaFarmaceutica())
                            .via(request.getVia())
                            .descripcion(request.getDescripcion())
                            .stockDisponible(request.getStockDisponible())
                            .unidadMedida(request.getUnidadMedida())
                            .activo(true)
                            .fechaCreacion(LocalDateTime.now())
                            .fechaActualizacion(LocalDateTime.now())
                            .build();
                    return repositorio.save(medicina);
                });
    }

    // ── Consultas ─────────────────────────────────────────────────────────────
    public Flux<MedicinaDto> findAll() {
        return repositorio.findAll()
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay medicamentos registrados en el sistema")));
    }

    public Flux<MedicinaDto> findActivos() {
        return repositorio.findByActivoTrue()
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay medicamentos activos en el sistema")));
    }

    public Mono<MedicinaDto> findById(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Medicamento no encontrado con ID: " + id)));
    }

    public Flux<MedicinaDto> buscarPorPrincipioActivo(String principioActivo) {
        return repositorio.findByPrincipioActivoIgnoreCaseContaining(principioActivo)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontraron medicamentos con principio activo: " + principioActivo)));
    }

    public Flux<MedicinaDto> findConStock() {
        return repositorio.findByStockDisponibleGreaterThan(0)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay medicamentos con stock disponible")));
    }

    // ── Actualizar ────────────────────────────────────────────────────────────
    public Mono<MedicinaDto> actualizar(String id, MedicinaRequest request) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Medicamento no encontrado con ID: " + id)))
                .flatMap(existing -> {
                    if (request.getNombre() != null)            existing.setNombre(request.getNombre());
                    if (request.getPrincipioActivo() != null)   existing.setPrincipioActivo(request.getPrincipioActivo());
                    if (request.getConcentracion() != null)     existing.setConcentracion(request.getConcentracion());
                    if (request.getFormaFarmaceutica() != null) existing.setFormaFarmaceutica(request.getFormaFarmaceutica());
                    if (request.getVia() != null)               existing.setVia(request.getVia());
                    if (request.getDescripcion() != null)       existing.setDescripcion(request.getDescripcion());
                    if (request.getStockDisponible() >= 0)      existing.setStockDisponible(request.getStockDisponible());
                    if (request.getUnidadMedida() != null)      existing.setUnidadMedida(request.getUnidadMedida());
                    existing.setFechaActualizacion(LocalDateTime.now());
                    return repositorio.save(existing);
                });
    }

    // ── Desactivar (soft-delete) ──────────────────────────────────────────────
    public Mono<MedicinaDto> desactivar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Medicamento no encontrado con ID: " + id)))
                .flatMap(medicina -> {
                    if (!medicina.isActivo()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El medicamento ya se encuentra inactivo"));
                    }
                    medicina.setActivo(false);
                    medicina.setFechaActualizacion(LocalDateTime.now());
                    return repositorio.save(medicina);
                });
    }

    // ── Eliminar permanente ───────────────────────────────────────────────────
    public Mono<Void> eliminar(String id) {
        return repositorio.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Medicamento no encontrado con ID: " + id)))
                .flatMap(m -> repositorio.deleteById(m.getId()));
    }
}