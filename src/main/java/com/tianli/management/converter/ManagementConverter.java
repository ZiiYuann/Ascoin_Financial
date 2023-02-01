package com.tianli.management.converter;


import com.tianli.chain.entity.Coin;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.entity.ServiceFee;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.HotWalletDetailedIoUQuery;
import com.tianli.management.vo.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagementConverter {

    FinancialWalletBoardVO toVO(FinancialBoardWallet financialWalletBoard);

    FinancialProductBoardVO toVO(FinancialBoardProduct FinancialProductBoard);

    BoardFinancialVO toFinancialProductBoardSummaryVO(FinancialBoardProduct financialBoardProduct);

    BoardWalletVO toFinancialWalletBoardSummaryVO(FinancialBoardWallet financialBoardWallet);

    MFinancialProductVO toMFinancialProductVO(FinancialProduct financialProduct);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    HotWalletDetailed toDO(HotWalletDetailedIoUQuery HotWalletDetailed);

    HotWalletDetailedVO toHotWalletDetailedVO(HotWalletDetailed hotWalletDetailed);

    BoardServiceFeeVO toServiceFeeVO(ServiceFee serviceFee);

    Coin toDO(CoinIoUQuery query);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    MUserListVO toManagerUserListVO(FinancialUserInfoVO financialUserInfoVO);

}
