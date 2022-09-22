package com.tianli.fund.convert;

import com.tianli.agent.management.vo.FundReviewVO;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.entity.FundReview;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.vo.FundIncomeRecordVO;
import com.tianli.fund.vo.FundProductVO;
import com.tianli.fund.vo.FundRecordVO;
import com.tianli.fund.vo.FundTransactionRecordVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FundRecordConvert {

    FundProductVO toProductVO(FinancialProduct financialProduct);

    FundRecordVO toFundVO(FundRecord fundRecord);

    FundIncomeRecordVO toFundIncomeVO(FundIncomeRecord fundIncomeRecord);

    FundTransactionRecordVO toFundTransactionVO(FundTransactionRecord fundTransactionRecord);

    FundReviewVO toReviewVO(FundReview fundReview);

}