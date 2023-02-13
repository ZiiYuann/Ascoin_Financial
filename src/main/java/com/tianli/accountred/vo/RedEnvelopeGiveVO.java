package com.tianli.accountred.vo;

import com.tianli.accountred.enums.RedEnvelopeChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-19
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeGiveVO {

    /**
     * 钱包id
     */
    private Long id;

    /**
     * 站外url
     */
    private RedEnvelopeChannel channel;

    private String externUrl;

}
