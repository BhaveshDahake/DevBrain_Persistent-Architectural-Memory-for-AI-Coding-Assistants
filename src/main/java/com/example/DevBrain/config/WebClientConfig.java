package com.example.DevBrain.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public WebClient cogneeWebClient(CogneeProperties properties) {
        
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(properties.getRequest().getTimeout())
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler((int) properties.getRequest().getTimeout().getSeconds()))
                        .addHandlerLast(new WriteTimeoutHandler((int) properties.getRequest().getTimeout().getSeconds())));

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(properties.getApi().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        String baseUrl = properties.getApi().getBaseUrl();
        String apiKey = properties.getApi().getKey();
        if (baseUrl != null && baseUrl.contains("cognee.ai") && apiKey != null && !apiKey.isEmpty()) {
            builder.defaultHeader("X-Api-Key", apiKey);
        }

        return builder
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("Sending [{}] to [{}]", clientRequest.method(), clientRequest.url());
            }
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("Received HTTP [{}] from API", clientResponse.statusCode().value());
            }
            return Mono.just(clientResponse);
        });
    }
}
