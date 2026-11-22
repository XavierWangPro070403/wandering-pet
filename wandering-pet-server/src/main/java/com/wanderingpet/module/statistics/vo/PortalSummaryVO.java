package com.wanderingpet.module.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门户首页数字统计出参（真实数据，无写死数字）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalSummaryVO {

    /** 在册待领养宠物数 */
    private Long availablePets;

    /** 累计领养完成数 */
    private Long adoptedPets;

    /** 已入驻救助站数 */
    private Long shelterCount;

    /** 覆盖城市数 */
    private Long cityCount;
}
