package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.aborrow.query.PledgeContextQuery;
import com.tianli.product.aborrow.vo.BorrowRecordPledgeVO;

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

    List<BorrowRecordPledgeDto> dtoListByUid(Long uid, Long bid);

    List<BorrowRecordPledgeVO> vos(Long uid, Long bid, PledgeType pledgeType);

    List<BorrowRecordPledge> listByUid(Long uid, Long bid);

    boolean releaseCompleted(Long uid, Long bid);

    void reduce(Long uid, Long bid, PledgeContextQuery query, boolean forced);

}
