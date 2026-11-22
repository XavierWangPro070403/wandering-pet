package com.wanderingpet.module.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.enums.PetStatus;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.module.adoption.entity.AdoptionApplication;
import com.wanderingpet.module.adoption.entity.AdoptionRecord;
import com.wanderingpet.module.adoption.mapper.AdoptionApplicationMapper;
import com.wanderingpet.module.adoption.mapper.AdoptionRecordMapper;
import com.wanderingpet.module.article.entity.Article;
import com.wanderingpet.module.article.mapper.ArticleMapper;
import com.wanderingpet.module.followup.entity.FollowUp;
import com.wanderingpet.module.followup.mapper.FollowUpMapper;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import com.wanderingpet.module.statistics.mapper.StatisticsMapper;
import com.wanderingpet.module.statistics.service.StatisticsService;
import com.wanderingpet.module.statistics.vo.AdminDashboardVO;
import com.wanderingpet.module.statistics.vo.PortalSummaryVO;
import com.wanderingpet.module.statistics.vo.ShelterWorkbenchVO;
import com.wanderingpet.module.statistics.vo.StatPointVO;
import com.wanderingpet.module.sys.entity.SysUser;
import com.wanderingpet.module.sys.mapper.SysUserMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计服务实现（聚合 SQL 走 StatisticsMapper.xml）
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;
    private final PetMapper petMapper;
    private final ShelterMapper shelterMapper;
    private final UserMapper userMapper;
    private final SysUserMapper sysUserMapper;
    private final AdoptionApplicationMapper applicationMapper;
    private final AdoptionRecordMapper recordMapper;
    private final FollowUpMapper followUpMapper;
    private final ArticleMapper articleMapper;
    private final NotificationService notificationService;

    @Override
    public PortalSummaryVO portalSummary() {
        Long available = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getStatus, PetStatus.AVAILABLE.name()));
        Long adopted = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getStatus, PetStatus.ADOPTED.name()));
        Long shelters = shelterMapper.selectCount(new LambdaQueryWrapper<Shelter>()
                .eq(Shelter::getAuditStatus, BizConstant.SHELTER_AUDIT_PASS));
        Long cities = shelterMapper.selectCount(new QueryWrapper<Shelter>()
                .select("DISTINCT city")
                .eq("audit_status", BizConstant.SHELTER_AUDIT_PASS));
        return new PortalSummaryVO(available, adopted, shelters, cities);
    }

    @Override
    public ShelterWorkbenchVO shelterWorkbench() {
        LoginUser user = UserContext.current();
        if (user == null || user.getShelterId() == null) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        Long shelterId = user.getShelterId();
        Long petTotal = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getShelterId, shelterId).ne(Pet::getStatus, PetStatus.OFFLINE.name()));
        Long available = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getShelterId, shelterId).eq(Pet::getStatus, PetStatus.AVAILABLE.name()));
        Long pendingApplication = applicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getShelterId, shelterId)
                .in(AdoptionApplication::getStatus, "SUBMITTED", "REVIEWING"));
        Long followUpPending = countShelterFollowUps(shelterId, "PENDING")
                + countShelterFollowUps(shelterId, "OVERDUE");
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Long monthAdopted = recordMapper.selectCount(new LambdaQueryWrapper<AdoptionRecord>()
                .eq(AdoptionRecord::getShelterId, shelterId)
                .ge(AdoptionRecord::getCreateTime, monthStart));
        return new ShelterWorkbenchVO(petTotal, available, pendingApplication, followUpPending, monthAdopted,
                notificationService.unreadCount());
    }

    private long countShelterFollowUps(Long shelterId, String status) {
        List<Long> recordIds = recordMapper.selectList(new LambdaQueryWrapper<AdoptionRecord>()
                        .eq(AdoptionRecord::getShelterId, shelterId).select(AdoptionRecord::getId))
                .stream().map(AdoptionRecord::getId).toList();
        if (recordIds.isEmpty()) {
            return 0;
        }
        return followUpMapper.selectCount(new LambdaQueryWrapper<FollowUp>()
                .in(FollowUp::getRecordId, recordIds).eq(FollowUp::getStatus, status));
    }

    @Override
    public AdminDashboardVO adminDashboard() {
        AdminDashboardVO vo = new AdminDashboardVO();
        // 总量
        Long petTotal = petMapper.selectCount(new LambdaQueryWrapper<Pet>());
        Long userTotal = userMapper.selectCount(new LambdaQueryWrapper<User>());
        Long applicationTotal = applicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>());
        Long adoptedTotal = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getStatus, PetStatus.ADOPTED.name()));
        Long shelters = shelterMapper.selectCount(new LambdaQueryWrapper<Shelter>()
                .eq(Shelter::getAuditStatus, BizConstant.SHELTER_AUDIT_PASS));
        Long articles = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, BizConstant.STATUS_PUBLISHED));
        vo.setTotals(List.of(
                new StatPointVO("petTotal", petTotal, "宠物档案总数"),
                new StatPointVO("adoptedPets", adoptedTotal, "已领养"),
                new StatPointVO("shelterCount", shelters, "入驻机构"),
                new StatPointVO("userCount", userTotal, "注册用户"),
                new StatPointVO("applicationCount", applicationTotal, "领养申请"),
                new StatPointVO("articleCount", articles, "发布文章")));
        vo.setAdoptionTrend(statisticsMapper.selectAdoptionTrend(6));
        vo.setCategoryDistribution(statisticsMapper.selectCategoryDistribution());
        vo.setCityDistribution(statisticsMapper.selectCityDistribution(8));
        vo.setFunnel(statisticsMapper.selectFunnel());
        return vo;
    }
}
