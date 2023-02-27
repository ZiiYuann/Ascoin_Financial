package com.tianli.accountred.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-13
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ORedEnvelopVO {

    private String nickname;

    private String coin;

    private String remarks;

    private int scale;
}
