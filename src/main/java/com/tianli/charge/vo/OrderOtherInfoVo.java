package com.tianli.charge.vo;

import com.tianli.account.vo.AccountUserTransferVO;
import lombok.Builder;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-21
 **/
@Data
@Builder
public class OrderOtherInfoVo {

    private Long transferExternalPk;

    private AccountUserTransferVO accountUserTransferVO;
}
