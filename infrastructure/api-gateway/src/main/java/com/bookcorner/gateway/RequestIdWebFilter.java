package com.bookcorner.gateway;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Gives every request a correlation ID ("X-Request-Id").
 * <ul>
 *   <li>The ID is passed to downstream services so one user action can be traced across many services' logs.</li>
 *   <li>It is also returned to the client, so a user can quote it when reporting a problem.</li>
 *   <li>A client-supplied ID is accepted only if it is short and made of safe characters,
 *       which prevents log injection (e.g. newlines or huge values in the header).</li>
 * </ul>
 * Runs for every request, including ones that match no route.
 */
@Component
public class RequestIdWebFilter implements WebFilter, Ordered {

    public static final String HEADER = "X-Request-Id";
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9-]{8,64}$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
        String requestId = (incoming != null && SAFE_ID.matcher(incoming).matches())
                ? incoming
                : UUID.randomUUID().toString();

        exchange.getResponse().getHeaders().set(HEADER, requestId);

        ServerWebExchange withId = exchange.mutate()
                .request(r -> r.headers(h -> h.set(HEADER, requestId)))
                .build();
        return chain.filter(withId);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
