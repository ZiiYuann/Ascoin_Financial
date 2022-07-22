package com.tianli.borrow.convert;


import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.vo.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BorrowConverter {

    BorrowCoinMainPageVO.BorrowOrder toMainVO(BorrowCoinOrder borrowCoinOrder);

    BorrowCoinOrderVO toVO(BorrowCoinOrder borrowCoinOrder);

    BorrowPledgeRecordVO toPledgeVO(BorrowPledgeRecord borrowPledgeRecord);

    BorrowInterestRecordVO toInterestVO(BorrowInterestRecord borrowInterestRecord);

    BorrowRepayRecordVO toRepayVO(BorrowRepayRecord borrowRepayRecord);

}
