package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.query.PledgeContextQuery;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
public interface BorrowRecordPledgeService extends IService<BorrowRecordPledge> {

    void save(Long uid, PledgeContextQuery query);

    List<BorrowRecordPledgeDto> dtoListByUid(Long uid);
}
