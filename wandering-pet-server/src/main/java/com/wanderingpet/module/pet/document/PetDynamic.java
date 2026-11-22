package com.wanderingpet.module.pet.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物动态（MongoDB pet_dynamic 集合）：救助站发布的宠物近况
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pet_dynamic")
@CompoundIndex(name = "idx_pet_create", def = "{'petId': 1, 'createTime': -1}")
public class PetDynamic {

    @Id
    private String id;

    private Long petId;

    private Long shelterId;

    private String content;

    private List<String> images;

    private Integer likeCount;

    private LocalDateTime createTime;
}
