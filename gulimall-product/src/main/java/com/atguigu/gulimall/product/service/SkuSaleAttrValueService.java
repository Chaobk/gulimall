package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Attr;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author chaobk
 * @email 1004945427@qq.com
 * @date 2023-03-18 21:34:49
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttrs(Long skuId, List<Attr> attr);
}

