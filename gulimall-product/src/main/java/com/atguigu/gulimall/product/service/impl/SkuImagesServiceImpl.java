package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.vo.Images;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuImagesDao;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.service.SkuImagesService;
import org.springframework.util.StringUtils;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveImages(Long skuId, List<Images> images) {
        // TODO 没有图片路径的无需保存
        List<SkuImagesEntity> collect = images.stream().map(image -> {
            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
            skuImagesEntity.setSkuId(skuId);
            skuImagesEntity.setImgUrl(image.getImgUrl());
            skuImagesEntity.setDefaultImg(image.getDefaultImg());
            return skuImagesEntity;
        }).filter(item -> !StringUtils.isEmpty(item.getImgUrl())).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}