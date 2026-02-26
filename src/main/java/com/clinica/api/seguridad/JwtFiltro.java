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

// ← SIN @Component, SIN @Service, SIN ninguna anotación de bean
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

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.esValido(token)) {
            return chain.filter(exchange);
        }

        String username = jwtUtil.obtenerUsername(token);

        return detallesServicio.findByUsername(username)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()))
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)));
    }
}