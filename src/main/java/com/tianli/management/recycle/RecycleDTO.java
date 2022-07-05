package com.tianli.management.recycle;

import lombok.Data;

import java.util.List;

/**
 * @Author cs
 * @Date 2022-03-28 10:15 上午
 */
@Data
public class RecycleDTO {
    private Long tokenId;
    private List<Long> uids;
}
