package com.saas.billing.api_gateway.filter;

import com.saas.billing.api_gateway.service.TokenCookieService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LogoutCookieFilter extends
        AbstractGatewayFilterFactory<LogoutCookieFilter.Config> {

    private final TokenCookieService tokenCookieService;

    public LogoutCookieFilter(TokenCookieService tokenCookieService) {
        super(Config.class);
        this.tokenCookieService = tokenCookieService;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return new OrderedGatewayFilter((exchange, chain) -> {

            System.out.println(">>> Logout request: "
                    + exchange.getRequest().getURI());

            exchange.getResponse().beforeCommit(() -> {

                System.out.println(">>> Response status: "
                        + exchange.getResponse().getStatusCode());

                if (exchange.getResponse().getStatusCode() == HttpStatus.NO_CONTENT) {
                    System.out.println(">>> clearCookies()");
                    tokenCookieService.clearCookies(exchange.getResponse());
                }

                return Mono.empty();
            });

            return chain.filter(exchange);

        }, -2);
    }

    public static class Config {}
}