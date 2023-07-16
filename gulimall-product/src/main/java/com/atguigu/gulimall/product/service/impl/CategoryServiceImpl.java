package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2.组装成父子的树形结构
        List<CategoryEntity> level1Menu = entities.stream().filter(c -> c.getParentCid() == 0)
                .map(m -> {
                    m.setChildren(getChildren(m, entities));
                    return m;
                })
                .sorted(Comparator.comparingInt(m -> (m.getSort() == null ? 0 : m.getSort())))
                .collect(Collectors.toList());


        return level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前的菜单是否被其他地方引用

        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[0]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    // TODO 产生堆外内存溢出：OutOfDirectMemoryError
    // 1) Springboot2.0以后默认使用Lettuce作为操作redis的客户端，使用netty进行网络通信
    // 2）Lettuce的bug导致netty堆外内存溢出 -Xmx300m：netty如果没有指定堆外内存，默认使用 -Xmx300m
    //  可以通过-Dio.netty.maxDirectMemory进行设置
    // 解决方案：不能使用-Dio.netty.maxDirectMemory支取调大堆外内存
    // 1）升级Lettuce客户端 2）切换使用jedis
    // redisTemplate
    // Lettuce、jedis操作redis的底层客户端。Spring再次封装
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /**
         * 1、空结果缓存，解决缓存穿透问题
         * 2、设置过期时间（加随机值），解决缓存雪崩
         * 3、加锁，解决缓存击穿
         */
        // 加入缓存逻辑，缓存中寸的数据是json字符串
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String json = ops.get("catalogJson");
        if (!StringUtils.isEmpty(json)) {
            System.out.println("命中缓存");
            return JSON.parseObject(json, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        }
        Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();

        return catalogJsonFromDb;
    }


    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        // 巍峨确保是自己的锁，嫁了个UUID
        String uuid = UUID.randomUUID().toString();
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Boolean lock = ops.setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功");
            // 加锁成功
            // 设置过期时间
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {

                // 占用成功后释放锁
                // 无法避免比对过程中锁失效的情况
//            String lockValue = ops.get("lock");
//            if (uuid.equals(lockValue)) {
//                // 删除自己的锁
//                redisTemplate.delete("lock");
//            }

                // 采用Lua脚本解锁，取自官方文档
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                Long executeRes = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDb;

        } else {
            // 枷锁失败...重试
            System.out.println("获取分布式锁失败，重试");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return getCatalogJsonFromDbWithLocalLock();
        }
    }


    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
//        Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
//        if (catalogJson != null) {
//            return catalogJson;
//        }
//        //查询数据库，再放到缓存
//        cache.put("catalogJson", res);
//

        // 只要是同一把锁，就能所著需要这个锁的所有线程
        // this？ SpringBoot所有的组件在容器中的组件都是单例的，可以锁
        synchronized (this) {
            //得到锁以后，我们应该去缓存中确定一次，如果没有才需要继续查询
            return getDataFromDb();
        }

    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {

            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        System.out.println("查询数据库");

        /**
         *  将多次查数据库，改为查一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1.查出所有1级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);
        ;

        // 2.封装数据
        Map<String, List<Catelog2Vo>> res = level1Categorys.stream().collect(Collectors.toMap(k -> {
            return k.getCatId().toString();
        }, v -> {
            // 1.每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> entities = getParentCid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(item2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item2.getCatId().toString(), item2.getName());
                    // 找当前二级分类的的三级分类封装成vo
                    List<CategoryEntity> categoryEntities  = getParentCid(selectList, item2.getCatId());
                    if (categoryEntities != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = categoryEntities.stream().map(item3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item2.getCatId().toString(), item3.getCatId().toString(), item3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        redisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(res), 1, TimeUnit.DAYS);
        return res;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1.收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
         if (!byId.getParentCid().equals(0L)) {
            findParentPath(byId.getParentCid(), paths);
        }
         return paths;
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                })
                .sorted(Comparator.comparingInt(m -> (m.getSort() == null ? 0 : m.getSort())))
                .collect(Collectors.toList());
        return children;
    }

}