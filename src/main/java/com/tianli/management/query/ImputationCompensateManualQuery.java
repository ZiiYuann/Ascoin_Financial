package com.tianli.management.query;

import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.TokenAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImputationCompensateManualQuery {

    private NetworkType network;

    private TokenAdapter tokenAdapter;

    private List<String> addresses;
}
