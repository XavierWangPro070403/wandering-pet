package com.wanderingpet.init;

import com.wanderingpet.module.chat.document.ChatMessage;
import com.wanderingpet.module.pet.document.PetDynamic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB 初始化：集合索引 + 演示聊天消息/宠物动态（仅集合为空时写入）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoInitRunner implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        try {
            // 索引（幂等）
            mongoTemplate.indexOps(ChatMessage.class)
                    .ensureIndex(new Index().on("sessionId", Sort.Direction.ASC).on("createTime", Sort.Direction.DESC)
                            .named("idx_session_create"));
            mongoTemplate.indexOps(PetDynamic.class)
                    .ensureIndex(new Index().on("petId", Sort.Direction.ASC).on("createTime", Sort.Direction.DESC)
                            .named("idx_pet_create"));
            mongoTemplate.indexOps(com.wanderingpet.common.document.OperationLog.class)
                    .ensureIndex(new Index().on("createTime", Sort.Direction.DESC).named("idx_create"));

            // 演示消息（对应 chat_session id=1：user1 ↔ shelter1）
            if (mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), ChatMessage.class) == 0) {
                LocalDateTime base = LocalDateTime.of(2026, 11, 15, 20, 20);
                saveMsg("1", 1L, "USER", 1L, "TEXT", "您好，我提交了橘子的领养申请，想先了解一下它的生活习惯。", null, base);
                saveMsg("1", 2L, "SHELTER", 1L, "TEXT", "您好，申请我们已经收到。橘子会用猫砂盆、饭点准时，已经完成绝育和疫苗。", null, base.plusMinutes(6));
                saveMsg("1", 1L, "USER", 1L, "TEXT", "周六上午方便到站里看看它吗？", null, base.plusMinutes(15));
                saveMsg("1", 2L, "SHELTER", 1L, "TEXT", "好的，周六上午我们等你们，记得带上封窗的照片。", null, base.plusMinutes(21));
                log.info("MongoDB 演示聊天消息已写入");
            }
            // 宠物动态
            if (mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), PetDynamic.class) == 0) {
                PetDynamic dynamic = PetDynamic.builder()
                        .petId(5L).shelterId(1L)
                        .content("煤球这周复查指标继续好转，精神不错，已经会主动蹭志愿者的手了。")
                        .images(List.of()).likeCount(6).createTime(LocalDateTime.of(2026, 11, 14, 16, 0))
                        .build();
                mongoTemplate.save(dynamic);
                log.info("MongoDB 演示宠物动态已写入");
            }
        } catch (Exception e) {
            log.warn("MongoDB 初始化失败（本地未启动 MongoDB 时属正常）: {}", e.getMessage());
        }
    }

    private void saveMsg(String sessionId, Long senderId, String senderType, Long receiverId,
                         String type, String content, String objectUrl, LocalDateTime time) {
        mongoTemplate.save(ChatMessage.builder()
                .sessionId(sessionId).senderId(senderId).senderType(senderType)
                .receiverId(receiverId).contentType(type).content(content)
                .objectUrl(objectUrl).isRead(1).createTime(time).build());
    }
}
