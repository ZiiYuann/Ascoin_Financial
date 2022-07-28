package com.tianli.borrow.convert;


import com.tianli.borrow.bo.BorrowOrderConfigBO;
import com.tianli.borrow.bo.BorrowPledgeCoinConfigBO;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.tianli.borrow.entity.BorrowPledgeCoinConfig;
import com.tianli.borrow.vo.BorrowApplePageVO;
import com.tianli.borrow.vo.BorrowPledgeCoinConfigVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BorrowCoinConfigConverter {

    BorrowApplePageVO toVO(BorrowCoinConfig borrowCoinConfig);

    BorrowCoinConfig toDO(BorrowOrderConfigBO bo);

    BorrowPledgeCoinConfig toPledgeDO(BorrowPledgeCoinConfigBO bo);

    BorrowPledgeCoinConfigVO toPledgeVO(BorrowPledgeCoinConfig coinConfig);

}
