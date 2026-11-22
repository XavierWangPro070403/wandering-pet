package com.wanderingpet.module.statistics.mapper;

import com.wanderingpet.module.statistics.vo.StatPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计 Mapper（聚合 SQL 全部走 XML）
 */
@Mapper
public interface StatisticsMapper {

    /** 近 N 个月领养完成趋势 */
    List<StatPointVO> selectAdoptionTrend(@Param("months") Integer months);

    /** 物种在册占比 */
    List<StatPointVO> selectCategoryDistribution();

    /** 待领养宠物城市分布 Top N */
    List<StatPointVO> selectCityDistribution(@Param("topN") Integer topN);

    /** 领养转化漏斗：各状态申请数 */
    List<StatPointVO> selectFunnel();
}
