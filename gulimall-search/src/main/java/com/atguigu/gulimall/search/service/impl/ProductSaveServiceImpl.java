package com.xunqi.gulimall.search.service.impl;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-06-06 16:54
 **/
//
//@Slf4j
//@Service("productSaveService")
//public class ProductSaveServiceImpl implements ProductSaveService {
//
//    @Autowired
//    private RestHighLevelClient esRestClient;
//
//    @Override
//    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
//
////1.在es中建立索引，建立号映射关系（doc/json/product-mapping.json）
//
//        //2. 在ES中保存这些数据
//        BulkRequest bulkRequest = new BulkRequest();
//        for (SkuEsModel skuEsModel : skuEsModels) {
//            //构造保存请求
//            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
//            indexRequest.id(skuEsModel.getSkuId().toString());
//            String jsonString = JSON.toJSONString(skuEsModel);
//            indexRequest.source(jsonString, XContentType.JSON);
//            bulkRequest.add(indexRequest);
//        }
//
//
//        BulkResponse bulk = esRestClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
//
//        //TODO 如果批量错误
//        boolean hasFailures = bulk.hasFailures();
//
//        List<String> collect = Arrays.asList(bulk.getItems()).stream().map(item -> {
//            return item.getId();
//        }).collect(Collectors.toList());
//
//        log.info("商品上架完成：{}",collect);
//
//        return hasFailures;
//    }
//}
