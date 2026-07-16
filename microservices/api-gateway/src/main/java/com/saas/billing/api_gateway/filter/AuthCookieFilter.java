package com.saas.billing.api_gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.api_gateway.service.TokenCookieService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AuthCookieFilter extends
        AbstractGatewayFilterFactory<AuthCookieFilter.Config> {

    private final TokenCookieService tokenCookieService;
    private final ObjectMapper objectMapper;

    public AuthCookieFilter(
            TokenCookieService tokenCookieService,
            ObjectMapper objectMapper) {
        super(Config.class);
        this.tokenCookieService = tokenCookieService;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    // Ce filtre intercepte les réponses
    // de /register et /login
    // extrait les tokens du body JSON
    // les emballe dans httpOnly cookies
    // retourne le body sans les tokens
    // ════════════════════════════════════

    @Override
    public GatewayFilter apply(Config config) {
        System.out.println(">>> AuthCookieFilter created");

        GatewayFilter filter = (exchange, chain) -> {
            System.out.println(">>> AuthCookieFilter executed: "
                    + exchange.getRequest().getMethod()
                    + " "
                    + exchange.getRequest().getURI());

            ServerHttpResponse originalResponse =
                    exchange.getResponse();

            // intercepter la réponse
            ServerHttpResponseDecorator decoratedResponse =
                    new ServerHttpResponseDecorator(originalResponse) {

                        @Override
                        public Mono<Void> writeWith(
                                org.reactivestreams.Publisher<? extends DataBuffer>
                                        body) {
                            System.out.println(">>> writeWith() called");

                            if (getStatusCode() != null
                                    && getStatusCode().is2xxSuccessful()
                                    && body instanceof Flux) {

                                System.out.println("Status = " + getStatusCode());
                                System.out.println("Body class = " + body.getClass().getName());

                                Flux<? extends DataBuffer> fluxBody =
                                        (Flux<? extends DataBuffer>) body;

                                return super.writeWith(
                                        fluxBody.map(dataBuffer -> {
                                            try {
                                                // lire le body JSON
                                                byte[] content = new byte[
                                                        dataBuffer.readableByteCount()
                                                        ];
                                                dataBuffer.read(content);
                                                DataBufferUtils.release(dataBuffer);

                                                String bodyStr = new String(
                                                        content,
                                                        StandardCharsets.UTF_8
                                                );

                                                // parser le JSON
                                                Map<String, Object> bodyMap =
                                                        objectMapper.readValue(
                                                                bodyStr, Map.class
                                                        );

                                                // extraire les tokens
                                                String accessToken = (String)
                                                        bodyMap.remove("accessToken");
                                                String refreshToken = (String)
                                                        bodyMap.remove("refreshToken");

                                                // ajouter dans les cookies
                                                if (accessToken != null) {
                                                    tokenCookieService.addJwtCookie(
                                                            originalResponse,
                                                            accessToken
                                                    );
                                                }
                                                if (refreshToken != null) {
                                                    tokenCookieService.addRefreshCookie(
                                                            originalResponse,
                                                            refreshToken
                                                    );
                                                }

                                                // retourner le body sans tokens
                                                byte[] newContent = objectMapper
                                                        .writeValueAsBytes(bodyMap);

                                                return exchange.getResponse()
                                                        .bufferFactory()
                                                        .wrap(newContent);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                // si erreur → retourner body original
                                                return dataBuffer;
                                            }
                                        })
                                );
                            }
                            return super.writeWith(body);
                        }

                        @Override
                        public Mono<Void> writeAndFlushWith(
                                org.reactivestreams.Publisher<? extends org.reactivestreams.Publisher<? extends DataBuffer>> body) {

                            System.out.println(">>> writeAndFlushWith() called");
                            return writeWith(Flux.from(body).flatMapSequential(p -> p));
                        }
                    };

            return chain.filter(
                    exchange.mutate()
                            .response(decoratedResponse)
                            .build()
            );
        };

        // ════════════════════════════════════
        // CRITIQUE : ordre -1 pour s'exécuter
        // AVANT NettyWriteResponseFilter
        // (sinon writeWith() n'est jamais
        // appelé et les cookies ne sont
        // jamais ajoutés à la réponse)
        // ════════════════════════════════════
        return new OrderedGatewayFilter(filter, -2);
    }

    public static class Config {}
}