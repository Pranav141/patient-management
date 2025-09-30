package com.pm.api_gateway.filter;

import com.pm.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationGatewayFilterFactory.class);
    private final JwtUtil jwtUtil;

    public JwtValidationGatewayFilterFactory( JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) ->{
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if(token == null || !token.startsWith("Bearer ")){
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                //send the response directly from here if token is not found or not in valid format
                return exchange.getResponse().setComplete();
            }

            boolean flag = jwtUtil.validateToken(token.substring(7));
            if(flag){
                Claims claims = jwtUtil.extractAllClaims(token.substring(7));
                String role = claims.get("role",String.class);
                exchange.getRequest().mutate().header("X-USER-ROLE",role).build();
                log.info("X-USER-ROLE:{}",role);
                return chain.filter(exchange);
            }
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();

//            return webClient.get()
//                    .uri("lb://auth-service/auth/validate")
//                    .header(HttpHeaders.AUTHORIZATION,token)
//                    .retrieve()
//                    .toBodilessEntity()
//                    .then(chain.filter(exchange))
//                    .onErrorResume(WebClientResponseException.class, ex -> {
//                        if (ex.getStatusCode().is4xxClientError()) {
//                            // Token is invalid
//                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                        }
//                        return exchange.getResponse().setComplete();
//                    });
        } ;
    }
}
