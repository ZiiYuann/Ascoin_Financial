package com.tianli.financial.convert;

import com.tianli.financial.entity.FinancialPurchaseRecord;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.vo.FinancialProductVO;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import org.mapstruct.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Mapper(componentModel = "spring")
public interface FinancialConverter {

    FinancialPurchaseResultVO toVO(FinancialPurchaseRecord financialProductPurchaseLog);

    FinancialProductVO toVO(FinancialProduct financialProduct);
}
