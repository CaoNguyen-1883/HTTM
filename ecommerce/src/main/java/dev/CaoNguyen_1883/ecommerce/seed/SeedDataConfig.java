package dev.CaoNguyen_1883.ecommerce.seed;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.seed")
@Data
public class SeedDataConfig {
    private boolean enabled = true;
    private boolean forceReseed = false;
}
