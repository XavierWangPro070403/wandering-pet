package com.wanderingpet.common.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 用户行为埋点（MongoDB user_behavior 集合）：浏览/收藏/申请等行为记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_behavior")
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_create", def = "{'userId': 1, 'createTime': -1}"),
        @CompoundIndex(name = "idx_target", def = "{'targetType': 1, 'targetId': 1}")
})
public class UserBehavior {

    @Id
    private String id;

    /** 用户 ID（游客为空） */
    private Long userId;

    /** 行为类型 VIEW-浏览 FAVORITE-收藏 APPLY-申请 */
    private String behaviorType;

    /** 对象类型 PET/ARTICLE */
    private String targetType;

    /** 对象 ID */
    private Long targetId;

    private LocalDateTime createTime;
}
