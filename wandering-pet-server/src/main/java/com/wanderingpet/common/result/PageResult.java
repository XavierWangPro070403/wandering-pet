package com.wanderingpet.common.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一分页结果
 *
 * @param <T> 记录类型
 */
@Data
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private List<T> list;
    private long total;
    private long pageNum;
    private long pageSize;
    private long pages;

    public static <E> PageResult<E> of(Page<E> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    /** MP 实体分页 → VO 分页转换 */
    public static <E, V> PageResult<V> of(Page<E> page, Function<E, V> converter) {
        List<V> list = page.getRecords().stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }
}
