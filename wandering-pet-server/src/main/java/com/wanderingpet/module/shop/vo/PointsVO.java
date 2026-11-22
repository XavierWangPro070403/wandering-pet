package com.wanderingpet.module.shop.vo;

import lombok.Data;

import java.util.List;

@Data
public class PointsVO {

    private Integer balance;

    private List<LogItem> logs;

    @Data
    public static class LogItem {
        private Long id;
        private Integer change;
        private Integer balance;
        private String type;
        private String remark;
        private String createTime;
    }
}
