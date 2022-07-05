package com.tianli.channel.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/20 16:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AfChannel extends Model<AfChannel> {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String data;

    private LocalDateTime create_time;

    private LocalDateTime update_time;
}
