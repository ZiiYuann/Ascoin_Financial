package com.tianli.accountred.dto;

import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.vo.RedEnvelopeExchangeCodeVO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-15
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopStatusDTO extends RedEnvelopeExchangeCodeVO {


    private LocalDateTime latestExpireTime;

    public RedEnvelopStatusDTO(RedEnvelopeStatus status) {
        super.setStatus(status);
    }

    public RedEnvelopStatusDTO(RedEnvelopeStatus status, LocalDateTime latestExpireTime) {
        this.latestExpireTime = latestExpireTime;
        super.setStatus(status);
    }
}
