package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        // 1.查出所有1级分类
        List<CategoryEntity> level1Categorys = getLevel1Categorys();

        // 2.封装数据
        Map<String, List<Catelog2Vo>> res = level1Categorys.stream().collect(Collectors.toMap(k -> {
            return k.getCatId().toString();
        }, v -> {
            // 1.每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(item2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item2.getCatId().toString(), item2.getName());
                    // 找当前二级分类的的三级分类封装成vo
                    List<CategoryEntity> categoryEntities  = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", item2.getCatId()));
                    if (categoryEntities != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = categoryEntities.stream().map(item3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item2.getCatId().toString(), item3.getCatId().toString(), item3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(Collections.singletonList(catelog3Vos));
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        return res;
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