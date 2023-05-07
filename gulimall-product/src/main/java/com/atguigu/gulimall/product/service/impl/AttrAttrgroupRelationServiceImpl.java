package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addRelation(Map<String, Object> params) {
        AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
        entity.setAttrId((Long) params.get("attrId"));
        entity.setAttrGroupId((Long) params.get("attrGroupId"));
        this.save(entity);
    }

    @Override
    public void saveRelationArr(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(vos).stream().map(v -> {
            AttrAttrgroupRelationEntity en = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(v, en);
            return en;
        }).collect(Collectors.toList());
        saveBatch(collect);
    }

}