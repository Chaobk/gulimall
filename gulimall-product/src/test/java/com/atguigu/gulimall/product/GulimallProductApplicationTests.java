package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.netflix.client.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径: {}", Arrays.asList(catelogPath));
    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功");

//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("垃圾");
//        brandService.updateById(brandEntity);

        List<BrandEntity> brandId = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        System.out.println(brandId);
    }

    @Test
    public void testRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("name", "zhangsan_" + UUID.randomUUID());

    }

    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }
}
