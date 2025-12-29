package dev.CaoNguyen_1883.ecommerce.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${spring.data.redis.timeout:3000}")
    private long timeout;

    @Value("${app.redis.namespace:ecommerce}")
    private String namespace;

    @Value("${spring.cache.type:redis}")
    private String cacheType;

    /**
     * Redis connection factory with connection pooling and timeouts
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);

        if(redisPassword != null && !redisPassword.trim().isEmpty()) {
            redisStandaloneConfiguration.setPassword(redisPassword);
        }

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(timeout))
                .keepAlive(true)
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .autoReconnect(true)
                .build();

        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .clientOptions(clientOptions)
                .build();
        return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
    }

    /**
     * Custom ObjectMapper specifically for Redis serialization
     * Handles Java 8 date/time types and polymorphic types safely
     * NOTE: This is separate from HTTP JSON serialization
     */
//    @Bean(name = "redisObjectMapper")
//    public ObjectMapper redisObjectMapper(){
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        // Enable default typing to preserve type information during serialization
//        // This prevents LinkedHashMap deserialization issues in Redis
//        mapper.activateDefaultTyping(
//            LaissezFaireSubTypeValidator.instance,
//            ObjectMapper.DefaultTyping.NON_FINAL,
//            JsonTypeInfo.As.PROPERTY
//        );
//
//        return mapper;
//    }

    /**
     * Redis template for manual Redis operations
     * Use this for custom cache logic, rate limiting, sessions, etc.
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    /**
     * Cache manager with different TTL strategies for different cache types
     * Supports both Redis and Caffeine (in-memory) based on spring.cache.type property
     */
    @Bean("cacheManager")
    @Primary
    public CacheManager cacheManager(
            @Autowired(required = false) RedisConnectionFactory connectionFactory,
            @Autowired(required = false) ObjectMapper redisObjectMapper) {

        // For development: Use Caffeine if cache type is set to "caffeine" to avoid Redis serialization issues
        if ("caffeine".equalsIgnoreCase(cacheType)) {
            log.info("Using Caffeine (in-memory) cache manager for development");
            return caffeineCacheManager();
        }

        // Verify Redis dependencies are available
        if (connectionFactory == null || redisObjectMapper == null) {
            log.warn("Redis dependencies not available, falling back to Caffeine cache");
            return caffeineCacheManager();
        }

        log.info("Using Redis cache manager for production");

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .prefixCacheNameWith(namespace + ":")
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Products - cache for 1 hour (frequently accessed, rarely updated)
        cacheConfigurations.put("products",
                defaultCacheConfig.entryTtl(Duration.ofHours(1)));

        // Product details - cache for 2 hours
        cacheConfigurations.put("productDetails",
                defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        // Categories - cache for 2 hours (rarely change)
        cacheConfigurations.put("categories",
                defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        // Brands - cache for 2 hours (rarely change)
        cacheConfigurations.put("brands",
                defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        // Users - cache for 15 minutes (security sensitive)
        cacheConfigurations.put("users",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("roles",
                defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        cacheConfigurations.put("permission",
                defaultCacheConfig.entryTtl(Duration.ofHours(2)));
        // User profiles - cache for 30 minutes
        cacheConfigurations.put("userProfiles",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));

        // Inventory/Stock - cache for 5 minutes (changes frequently)
        cacheConfigurations.put("inventory",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));

        // Orders - cache for 10 minutes
        cacheConfigurations.put("orders",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));

        // Shopping cart - cache for 1 hour
        cacheConfigurations.put("cart",
                defaultCacheConfig.entryTtl(Duration.ofHours(1)));

        // Search results - cache for 15 minutes
        cacheConfigurations.put("searchResults",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));


        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * Caffeine cache manager as an alternative for development
     * Avoids Redis serialization issues with Spring DevTools
     */
    private CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "products", "productDetails", "categories", "brands", "users", "userProfiles",
                "inventory", "orders", "cart", "searchResults", "roles", "permission"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }

    /**
     * Expose namespace as a bean for use in other components
     */
    @Bean
    public String redisNamespace() {
        return namespace + ":";
    }
}

