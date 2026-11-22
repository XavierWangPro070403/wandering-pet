package com.wanderingpet.module.pet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.pet.dto.FavoriteDTO;
import com.wanderingpet.module.pet.vo.DynamicVO;

import java.util.List;

/**
 * 收藏服务 + 宠物动态服务
 */
public interface FavoriteService {

    /** 收藏（幂等） */
    void favorite(FavoriteDTO dto);

    /** 取消收藏（幂等） */
    void unfavorite(String targetType, Long targetId);

    /** 我的宠物收藏分页 */
    PageResult<com.wanderingpet.module.pet.vo.FavoritePetVO> pageMyPetFavorites(Integer pageNum, Integer pageSize);

    /** 是否已收藏（当前登录用户） */
    boolean isFavorited(String targetType, Long targetId);

    // ---------- 宠物动态（MongoDB） ----------

    List<DynamicVO> listDynamics(Long petId, Integer limit);

    Long saveDynamic(Long petId, String content, List<String> images);
}
