package com.wanderingpet.module.shop.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private String status;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private Integer pointsEarned;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String createTime;

    private String payTime;

    private List<Item> items;

    @Data
    public static class Item {
        private Long id;
        private Long productId;
        private String productName;
        private String productCover;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
