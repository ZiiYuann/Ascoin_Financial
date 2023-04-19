package com.tianli.account.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUserTransfer {

    @Id
    private Long id;

    private Long transferUid;

    private Long transferChatId;

    private Long receiveUid;

    private Long receiveChatId;

    private String coin;

    private BigDecimal amount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String transferOrderNo;

    private Long externalPk;
}
