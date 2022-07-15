package com.tianli.financial.convert;

import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.vo.FinancialProductVO;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.management.query.FinancialProductEditQuery;
import org.mapstruct.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Mapper(componentModel = "spring")
public interface FinancialConverter {

    FinancialPurchaseResultVO toVO(FinancialRecord financialProductPurchaseLog);

    FinancialProductVO toVO(FinancialProduct financialProduct);

    FinancialProduct toDO(FinancialProductEditQuery financialProductQuery);
}
