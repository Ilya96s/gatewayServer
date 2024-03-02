package com.optimagrowth.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Component
@Slf4j
public class TrackingFilter implements GlobalFilter {

    @Autowired
    private FilterUtils filterUtils;

    /**
     * Извлекает HTTPзаголовок из запроса с помощью объекта
     * ServerWebExchange, переданного в метод filter() в виде параметра
     *
     * Код, который выполняется каждый раз,когда запрос проходит через фильтр
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        if (isCorrelationIdPresent(requestHeaders)) {
            log.debug("tmx-correlation-id found in tracking filter: {}. ",
                    filterUtils.getCorrelationId(requestHeaders));
        } else {
            String correlationID = generateCorrelationId();
            exchange = filterUtils.setCorrelationId(exchange, correlationID);
            log.debug("tmx-correlation-id generated in tracking filter: {}.", correlationID);
        }

        return chain.filter(exchange);
    }

    /**
     * Вспомогательный метод, проверяющий наличие идентификатора
     * корреляции в заголовке запроса
     * @param requestHeaders
     * @return
     */
    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
        if (filterUtils.getCorrelationId(requestHeaders) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Вспомогательный метод, генерирующий новое значение UUID для
     * использования в качестве идентификатора корреляции
     *
     * @return
     */
    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }

}
