package com.atguigu.gulimall.product.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.security.SecureRandom;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class MyCacheConfig {

        /**
         * 配置文件中的ttl失效了
         *
         * 是因为配置文件的东西没有用上
         * 1、原来和配置文件绑定的配置类是这样子的
         *      @ConfigurationProperties(prefix = "spring.cache")
         *      public class CacheProperties
         * 2、要让配置生效
         *      @EnableConfigurationProperties(CacheProperties.class)
         *
         *
         * @return
         */
        @Bean
        RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

                config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
                config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));


                // 将配置文件的配置生效，否则配置文件中的ttl就没有用
                CacheProperties.Redis redisProperties = cacheProperties.getRedis();
                if (redisProperties.getTimeToLive() != null) {
                        config = config.entryTtl(redisProperties.getTimeToLive());
                }

                if (redisProperties.getKeyPrefix() != null) {
                        config = config.prefixKeysWith(redisProperties.getKeyPrefix());
                }

                if (!redisProperties.isCacheNullValues()) {
                        config = config.disableCachingNullValues();
                }

                if (!redisProperties.isUseKeyPrefix()) {
                        config = config.disableKeyPrefix();
                }

                return config;
        }

}
