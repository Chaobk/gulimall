package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {

        private String keyword;

        private Long catalog3Id;

        /**
         * 排序条件
         * sort=saleCount_asc/desc
         * sort=skuPrice_asc/desc
         * sort=hostScore_asc/desc
         */
        private String sort;

        /**
         * 过滤条件
         *   hasStock（是否有货）、skuPrice区间、brandId、catalog3Id、attr
         *   hasStock=0/1
         */
        private Integer hasStock;
        private String skuPrice;
        private List<Long> brandId; // 按照品牌进行查询，可以多选
        private List<String> attrs;
        private Integer pageNum;


}
