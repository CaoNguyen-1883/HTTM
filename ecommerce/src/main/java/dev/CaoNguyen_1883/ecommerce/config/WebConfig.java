package dev.CaoNguyen_1883.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Web configuration for HTTP JSON serialization
 * This ObjectMapper is used for REST API request/response handling
 */
@Configuration
public class WebConfig {

    /**
     * Primary ObjectMapper for HTTP JSON serialization
     * Does NOT use default typing (no @class property required)
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.build();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // DO NOT enable default typing for HTTP requests
        // This allows clean JSON without @class properties

        return mapper;
    }
}
