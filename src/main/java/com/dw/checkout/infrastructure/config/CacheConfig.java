package com.dw.checkout.infrastructure.config;

import com.dw.checkout.domain.model.PaymentMethod;
import com.dw.checkout.domain.model.Product;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCTS_CACHE = "products";
    public static final String PAYMENT_METHODS_CACHE = "paymentMethods";

    @Bean
    public RedisCacheConfiguration defaultRedisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                );
    }

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                          RedisCacheConfiguration defaultRedisCacheConfiguration) {
        Jackson2JsonRedisSerializer<Product> productSerializer =
                new Jackson2JsonRedisSerializer<>(Product.class);
        ObjectMapper paymentMethodMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CollectionType paymentMethodsType = paymentMethodMapper.getTypeFactory()
                .constructCollectionType(List.class, PaymentMethod.class);
        Jackson2JsonRedisSerializer<List<PaymentMethod>> paymentMethodSerializer =
                new Jackson2JsonRedisSerializer<>(paymentMethodMapper, paymentMethodsType);

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                PRODUCTS_CACHE, defaultRedisCacheConfiguration
                        .entryTtl(Duration.ofMinutes(15))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(productSerializer)),
                PAYMENT_METHODS_CACHE, defaultRedisCacheConfiguration
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(paymentMethodSerializer))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultRedisCacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
