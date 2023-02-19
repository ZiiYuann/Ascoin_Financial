package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import com.tianli.product.aborrow.query.PledgeContextQuery;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
public interface BorrowRecordPledgeService extends IService<BorrowRecordPledge> {

    void save(Long uid, Long bid, PledgeContextQuery query);

    void save(Long uid, Long bid, PledgeContextQuery query, ModifyPledgeContextType type);

    /**
     * 释放质押物
     */
    void release(Long uid, Long bid);

    List<BorrowRecordPledgeDto> dtoListByUid(Long uid);

    List<BorrowRecordPledge> listByUid(Long uid);

    boolean releaseCompleted(Long uid,Long bid);

}
