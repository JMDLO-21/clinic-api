package com.clinica.api.seguridad;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

// Primero preguntamos si la ruta es pública (no requiere autenticación).
@RequiredArgsConstructor
public class JwtFiltro implements WebFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetallesServicio detallesServicio;

    private static final List<String> RUTAS_PUBLICAS = List.of(
            "/api/auth/",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        boolean esRutaPublica = RUTAS_PUBLICAS.stream().anyMatch(path::startsWith);

        if (esRutaPublica) {
            return chain.filter(exchange);
        }
// Luego preguntamos si tiene un header Authorization con formato Bearer. Si no, dejamos pasar el request sin autenticación (será bloqueado por Spring Security).
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
// Si tiene un token, lo validamos. Si no es válido, dejamos pasar el request sin autenticación (será bloqueado por Spring Security).
        String token = authHeader.substring(7);

        if (!jwtUtil.esValido(token)) {
            return chain.filter(exchange);
        }

        String username = jwtUtil.obtenerUsername(token);
// Si el token es válido, cargamos los detalles del usuario y lo autenticamos en el contexto de seguridad.
        return detallesServicio.findByUsername(username)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()))
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)));
    }
}