package com.tianli.product.afund.convert;

import com.tianli.agent.management.vo.FundReviewVO;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afund.entity.FundIncomeRecord;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.entity.FundReview;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.tianli.product.afund.vo.FundIncomeRecordVO;
import com.tianli.product.afund.vo.FundProductVO;
import com.tianli.product.afund.vo.FundRecordVO;
import com.tianli.product.afund.vo.FundTransactionRecordVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FundRecordConvert {

    FundProductVO toProductVO(FinancialProduct financialProduct);

    FundRecordVO toFundVO(FundRecord fundRecord);

    FundIncomeRecordVO toFundIncomeVO(FundIncomeRecord fundIncomeRecord);

    FundTransactionRecordVO toFundTransactionVO(FundTransactionRecord fundTransactionRecord);

    FundReviewVO toReviewVO(FundReview fundReview);

}
