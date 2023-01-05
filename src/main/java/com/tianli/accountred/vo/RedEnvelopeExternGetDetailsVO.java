package com.tianli.accountred.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-03
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeExternGetDetailsVO {

    /**
     * 红包持有者名称
     */
    private Long uid;

    private Long shortUid;

    /**
     * 币别
     */
    private String coin;

    private String coinUrl;

    /**
     * 红包的总金额
     */
    private BigDecimal totalAmount;

    /**
     * 红包数量
     */
    private int num;

    private int receiveNum;

    /**
     * 红包文案
     */
    private String remarks;

    /**
     * WAIT:等待发送 PROCESS:发送中 fAIL:发送失败(上链失败) FINISH:已经完成 OVERDUE:过期
     */
    private RedEnvelopeStatus status;

    private BigDecimal usdtRate;

    private LocalDateTime expireTime;

    // 这里的record是基于缓存的记录，并不是真正的领取记录
    private IPage<RedEnvelopeExternGetRecordVO> recordPage;

}
