package com.tianli.management.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 手动归集query
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletImputationManualQuery {

    private List<Long> imputationIds;

}
