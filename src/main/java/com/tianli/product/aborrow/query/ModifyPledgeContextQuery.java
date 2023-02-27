package com.tianli.product.aborrow.query;

import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-16
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyPledgeContextQuery {

    @NotNull
    private List<PledgeContextQuery> pledgeContext;

    @NotNull
    private ModifyPledgeContextType type;

}
