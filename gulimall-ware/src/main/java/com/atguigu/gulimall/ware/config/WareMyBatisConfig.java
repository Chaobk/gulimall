package com.atguigu.gulimall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.atguigu.gulimall.ware.dao")
public class WareMyBatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大也后操作，true调回到首页，false继续请求，默认false
        paginationInterceptor.setOverflow(true);
        // 设置最大但也限制数量，默认500条，-1不受限制
        paginationInterceptor.setLimit(500);
        return  paginationInterceptor;
    }
}
