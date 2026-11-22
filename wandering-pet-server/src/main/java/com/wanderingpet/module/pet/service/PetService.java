package com.wanderingpet.module.pet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.pet.dto.PetQueryDTO;
import com.wanderingpet.module.pet.dto.PetSaveDTO;
import com.wanderingpet.module.pet.dto.PetStatusDTO;
import com.wanderingpet.module.pet.vo.AdminPetVO;
import com.wanderingpet.module.pet.vo.PetDetailVO;
import com.wanderingpet.module.pet.vo.PetListVO;
import com.wanderingpet.module.pet.vo.PetManageListVO;

import java.util.List;

/**
 * 宠物档案服务
 */
public interface PetService {

    /** 门户/小程序分页（默认 AVAILABLE，登录后返回收藏标记） */
    PageResult<PetListVO> pagePortal(PetQueryDTO query);

    /** 宠物详情（Redis 缓存 30 分钟，空值缓存 60 秒防穿透） */
    PetDetailVO detail(Long id);

    /** 相似推荐（同物种、可领养，按收藏数倒序） */
    List<PetListVO> similar(Long petId, Integer limit);

    /** 记录一次浏览（浏览量 + 日榜 + 行为埋点） */
    void recordView(Long petId);

    /** 机构端本机构分页（全状态） */
    PageResult<PetManageListVO> pageShelter(PetQueryDTO query);

    /** 管理端全量分页 */
    PageResult<AdminPetVO> pageAdmin(PetQueryDTO query);

    /** 发布宠物档案（SHELTER） */
    Long save(PetSaveDTO dto);

    /** 编辑宠物档案（SHELTER，归属校验） */
    void update(Long id, PetSaveDTO dto);

    /** 删除宠物档案（SHELTER，同步清理 MinIO 图片） */
    void delete(Long id);

    /** 上下架/治疗状态变更（SHELTER 单个或批量） */
    void changeStatus(PetStatusDTO dto);
}
