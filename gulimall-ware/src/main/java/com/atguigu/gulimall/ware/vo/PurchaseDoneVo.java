package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseDoneVo {
    /**
     * 采购单id
     */
    private Long id;

    private List<PurchaseItemDoneVo> items;
}
