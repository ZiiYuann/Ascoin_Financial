package com.tianli.fund.convert;

import com.tianli.financial.entity.FinancialProduct;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.vo.FundIncomeRecordVO;
import com.tianli.fund.vo.FundProductVO;
import com.tianli.fund.vo.FundRecordVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FundRecordConvert {

    FundProductVO toProductVO(FinancialProduct financialProduct);

    FundRecordVO toFundVO(FundRecord fundRecord);

    FundIncomeRecordVO toFundIncomeVO(FundIncomeRecord fundIncomeRecord);

}
