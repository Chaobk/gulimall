package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

public class SearchResult {

    private List<SkuEsModel> products;
    private Integer pageNum;
    private Long total;
    private Integer totalPages;

    private List<BrandVo> brands;

    private List<CatalogVo> catalogs;

    private List<AttrVo> attrs;

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
        private String brandImg;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}
