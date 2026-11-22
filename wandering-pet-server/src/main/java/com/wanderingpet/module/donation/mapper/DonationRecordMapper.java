package com.wanderingpet.module.donation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.donation.vo.DonorRankVO;
import org.apache.ibatis.annotations.Mapper;
import com.wanderingpet.module.donation.entity.DonationRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 捐赠明细 Mapper（捐赠榜走 XML）
 */
@Mapper
public interface DonationRecordMapper extends BaseMapper<DonationRecord> {

    /** 捐赠榜：按捐赠人聚合金额取 Top N（匿名单独成行） */
    List<DonorRankVO> selectDonorRank(@Param("projectId") Long projectId, @Param("topN") Integer topN);
}
