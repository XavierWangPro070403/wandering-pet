package com.wanderingpet.module.statistics.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理端数据看板出参
 */
@Data
public class AdminDashboardVO {

    /** 总量：availablePets/adoptedPets/petTotal/shelterCount/userCount/applicationCount/articleCount/donationAmount */
    private List<StatPointVO> totals;

    /** 近 6 个月领养完成趋势（月份-数量） */
    private List<StatPointVO> adoptionTrend;

    /** 物种在册占比 */
    private List<StatPointVO> categoryDistribution;

    /** 待领养宠物城市分布 Top 8 */
    private List<StatPointVO> cityDistribution;

    /** 领养转化漏斗：提交→审核→面谈/家访→通过→完成 */
    private List<StatPointVO> funnel;
}
