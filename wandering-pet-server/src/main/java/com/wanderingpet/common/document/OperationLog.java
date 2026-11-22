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
 * 操作日志（MongoDB operation_log 集合）：由 @OpLog 注解切面异步写入
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "operation_log")
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_create", def = "{'userId': 1, 'createTime': -1}"),
        @CompoundIndex(name = "idx_create", def = "{'createTime': -1}")
})
public class OperationLog {

    @Id
    private String id;

    /** 操作人 ID */
    private Long userId;

    /** 操作人类型 ADMIN/SHELTER/USER */
    private String userType;

    /** 业务模块 */
    private String module;

    /** 操作动作 */
    private String action;

    /** 请求方法（类名#方法名） */
    private String method;

    /** 请求参数（JSON，超长截断） */
    private String params;

    /** 客户端 IP */
    private String ip;

    /** 耗时（毫秒） */
    private Long duration;

    /** 是否成功 */
    private Boolean success;

    /** 错误信息（失败时记录） */
    private String errorMessage;

    private LocalDateTime createTime;
}
