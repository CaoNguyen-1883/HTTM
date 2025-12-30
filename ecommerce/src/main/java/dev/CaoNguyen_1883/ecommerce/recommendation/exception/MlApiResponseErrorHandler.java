package dev.CaoNguyen_1883.ecommerce.recommendation.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Slf4j
public class MlApiResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is5xxServerError()) {
            log.error("ML API server error: {}", response.getStatusCode());
        } else if (response.getStatusCode().is4xxClientError()) {
            log.warn("ML API client error: {}", response.getStatusCode());
        }
        // Don't throw exception - let service handle gracefully with fallback
    }
}
