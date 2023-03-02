package com.tianli.other.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-03-02
 **/
@Data
@NoArgsConstructor
public class PushMessageVO {

    private Long id;

    private Long uid;

    private String title;

    private String content;

    private LocalDateTime createTime;

}
