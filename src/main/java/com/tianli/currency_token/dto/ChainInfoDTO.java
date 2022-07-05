package com.tianli.currency_token.dto;

import com.tianli.currency_token.mapper.ChainType;
import com.tianli.tool.MapTool;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class ChainInfoDTO {
    private ChainType chain;
    private String address;
    private String min_recharge;
    private Object chain_info;
    private String withdraw_fixed_amount;
    private String withdraw_rate;
    private String withdraw_min_amount;

    public static Map<String, Object> chainInfos = MapTool.Map()
            .put("erc20", MapTool.Map().put("full_name", "Ethereum(ERC20)").put("recharge_confirm_block", 12).put("withdraw_confirm_block", 12))
            .put("trc20", MapTool.Map().put("full_name", "Tron(TRC20)").put("recharge_confirm_block", 1).put("withdraw_confirm_block", 1))
            .put("bep20", MapTool.Map().put("full_name", "BNB Smart Chain(BEP20)").put("recharge_confirm_block", 15).put("withdraw_confirm_block", 15));

}
