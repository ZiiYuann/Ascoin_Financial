package com.tianli.management.query;

import lombok.Data;

import java.util.List;

/**
 * 手动归集query
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Data
public class WalletImputationManualQuery {

    private List<Long> imputationIds;

}
