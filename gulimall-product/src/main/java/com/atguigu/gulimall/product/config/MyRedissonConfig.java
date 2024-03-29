package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    /**
     * 所有对Redisson的使用都是通过RedissonClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        // 1.创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://185.242.235.202:6379");
        config.useSingleServer().setPassword("redis@123");
        // 2.根据Config创建实例
        return Redisson.create(config);
    }


}
