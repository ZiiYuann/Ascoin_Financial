package com.tianli.management.converter;


import com.tianli.management.entity.FinancialWalletBoard;
import com.tianli.management.vo.FinancialWalletBoardVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagementConverter {

    FinancialWalletBoardVO toVO(FinancialWalletBoard financialWalletBoard);
}
