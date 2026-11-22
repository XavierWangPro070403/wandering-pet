package com.wanderingpet.module.auth.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 账号合并数据迁移 Mapper（账号绑定串联时使用）
 *
 * <p>绑定手机号/微信发生凭据冲突时，把从账号的业务数据整体迁移到主账号，
 * 之后从账号逻辑删除。表名均为常量，无注入风险。</p>
 */
@Mapper
public interface AccountMergeMapper {

    // ---------- 迁移前去重（避免撞唯一索引） ----------

    /** 双方对同一宠物都存在进行中的申请时无法迁移（uk_pet_user_active 冲突），调用方应先提示用户处理 */
    @Select("SELECT COUNT(*) FROM adoption_application f "
            + "WHERE f.user_id = #{from} AND f.active_flag = 1 "
            + "AND EXISTS (SELECT 1 FROM adoption_application t "
            + "            WHERE t.user_id = #{to} AND t.pet_id = f.pet_id AND t.active_flag = 1)")
    int countConflictingActiveApplications(@Param("from") Long from, @Param("to") Long to);

    /** 从账号与主账号收藏了同一目标时，丢弃从账号的重复收藏 */
    @Delete("DELETE f FROM favorite f "
            + "INNER JOIN favorite k ON k.user_id = #{to} AND k.target_type = f.target_type "
            + "AND k.target_id = f.target_id "
            + "WHERE f.user_id = #{from}")
    int dropDuplicateFavorites(@Param("from") Long from, @Param("to") Long to);

    /** 从账号与主账号存在同一机构的会话时，丢弃从账号的重复会话（其历史消息随会话一并失效） */
    @Delete("DELETE cs FROM chat_session cs "
            + "INNER JOIN chat_session k ON k.user_id = #{to} AND k.shelter_id = cs.shelter_id "
            + "WHERE cs.user_id = #{from}")
    int dropDuplicateChatSessions(@Param("from") Long from, @Param("to") Long to);

    // ---------- 业务数据整体迁移 ----------

    @Update("UPDATE adoption_application SET user_id = #{to} WHERE user_id = #{from}")
    int migrateApplications(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE adoption_record SET user_id = #{to} WHERE user_id = #{from}")
    int migrateRecords(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE favorite SET user_id = #{to} WHERE user_id = #{from}")
    int migrateFavorites(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE notification SET user_id = #{to} WHERE user_id = #{from}")
    int migrateNotifications(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE chat_session SET user_id = #{to} WHERE user_id = #{from}")
    int migrateChatSessions(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE comment SET user_id = #{to} WHERE user_id = #{from}")
    int migrateComments(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE donation_record SET user_id = #{to} WHERE user_id = #{from}")
    int migrateDonations(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE rescue_report SET user_id = #{to} WHERE user_id = #{from}")
    int migrateRescueReports(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE volunteer_application SET user_id = #{to} WHERE user_id = #{from}")
    int migrateVolunteers(@Param("from") Long from, @Param("to") Long to);

    @Update("UPDATE user_address SET user_id = #{to} WHERE user_id = #{from}")
    int migrateAddresses(@Param("from") Long from, @Param("to") Long to);
}
