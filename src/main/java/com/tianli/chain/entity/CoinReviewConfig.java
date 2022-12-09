package com.tianli.chain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-08
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoinReviewConfig {

    @Id
    private Long id;

    /**
     * 系统自动审核，自动打币
     */
    private int autoReviewAutoTransfer;

    /**
     * 人工审核，人工打币
     */
    private int manualReviewManualTransfer;

    /**
     * 时间限制
     */
    private int hourLimit;

    /**
     * 次数限制
     */
    private int timesLimit;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

}
