package dev.CaoNguyen_1883.ecommerce.config;

import dev.CaoNguyen_1883.ecommerce.recommendation.exception.MlApiResponseErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${ml.recommendation.api.url:http://localhost:8001}")
    private String mlApiUrl;

    @Value("${ml.recommendation.api.timeout:5000}")
    private int timeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(mlApiUrl)
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .errorHandler(new MlApiResponseErrorHandler())
                .build();
    }
}
