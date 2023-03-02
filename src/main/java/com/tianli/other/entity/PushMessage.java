package com.tianli.other.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-03-02
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushMessage {

    @TableId
    private Long id;

    private Long uid;

    private String title;

    private String content;

    private LocalDateTime createTime;

}
