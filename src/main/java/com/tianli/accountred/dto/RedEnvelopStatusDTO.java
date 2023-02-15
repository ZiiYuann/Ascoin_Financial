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

    private RedEnvelopeStatus status;

    private LocalDateTime latestExpireTime;

    public RedEnvelopStatusDTO(RedEnvelopeStatus status) {
        this.status = status;
    }
}
