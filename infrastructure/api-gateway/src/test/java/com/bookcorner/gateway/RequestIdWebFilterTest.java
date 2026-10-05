package com.bookcorner.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class RequestIdWebFilterTest {

    private final RequestIdWebFilter filter = new RequestIdWebFilter();

    private record Result(ServerWebExchange original, ServerWebExchange passedDownstream) { }

    private Result run(MockServerHttpRequest request) {
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<ServerWebExchange> seen = new AtomicReference<>();
        filter.filter(exchange, e -> {
            seen.set(e);
            return Mono.empty();
        }).block();
        return new Result(exchange, seen.get());
    }

    @Test
    void generatesAnIdWhenTheClientSendsNone() {
        Result r = run(MockServerHttpRequest.get("/anything").build());

        String id = r.original().getResponse().getHeaders().getFirst(RequestIdWebFilter.HEADER);
        assertThat(id).isNotBlank();
        assertThat(r.passedDownstream().getRequest().getHeaders().getFirst(RequestIdWebFilter.HEADER))
                .isEqualTo(id);
    }

    @Test
    void keepsAValidClientSuppliedId() {
        Result r = run(MockServerHttpRequest.get("/anything")
                .header(RequestIdWebFilter.HEADER, "abc-12345678").build());

        assertThat(r.original().getResponse().getHeaders().getFirst(RequestIdWebFilter.HEADER))
                .isEqualTo("abc-12345678");
    }

    @Test
    void replacesAnUnsafeClientSuppliedId() {
        Result r = run(MockServerHttpRequest.get("/anything")
                .header(RequestIdWebFilter.HEADER, "bad id with spaces!").build());

        String id = r.original().getResponse().getHeaders().getFirst(RequestIdWebFilter.HEADER);
        assertThat(id).isNotEqualTo("bad id with spaces!");
        assertThat(id).matches("^[A-Za-z0-9-]{8,64}$");
    }
}
