package com.tianli.exchange.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 交易消息处理失败表
 * </p>
 *
 * @author lzy
 * @since 2022-07-01
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@TableName("exchange_msg_fail")
public class ExchangeMsgFail extends Model<ExchangeMsgFail> {

    private static final long serialVersionUID=1L;

    private Long id;

    /**
     * 失败的消息内容
     */
    private String message;

    /**
     * 消息id
     */
    private Long msg_id;

    /**
     * 失败原因
     */
    private String fail_reason;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    private Integer retry_number;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
