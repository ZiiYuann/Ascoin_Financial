package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.enums.PledgeStatus;
import com.tianli.product.aborrow.mapper.BorrowRecordMapper;
import com.tianli.product.aborrow.query.BorrowUserQuery;
import com.tianli.product.aborrow.service.BorrowInterestService;
import com.tianli.product.aborrow.service.BorrowRecordCoinService;
import com.tianli.product.aborrow.service.BorrowRecordPledgeService;
import com.tianli.product.aborrow.service.BorrowRecordService;
import com.tianli.product.aborrow.vo.MBorrowUserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord>
        implements BorrowRecordService {

    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private BorrowRecordCoinService borrowRecordCoinService;
    @Resource
    private BorrowRecordPledgeService borrowRecordPledgeService;
    @Resource
    private BorrowInterestService borrowInterestService;

    @Override
    @Transactional
    public BorrowRecord getWait(Long bid) {

        BorrowRecord borrowRecord = this.getById(bid);

        BorrowRecord newRecord = this.getOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUid, borrowRecord.getUid())
                .eq(BorrowRecord::getPledgeStatus, PledgeStatus.WAIT)
                .eq(BorrowRecord::getParentId, bid));
        if (Objects.isNull(newRecord)) {
            LocalDateTime now = LocalDateTime.now();
            borrowRecord.setId(null);
            borrowRecord.setCreateTime(now);
            borrowRecord.setUpdateTime(now);
            borrowRecord.setPledgeStatus(PledgeStatus.WAIT);
            borrowRecord.setParentId(bid);
            this.save(borrowRecord);
            newRecord = borrowRecord;

        }
        return borrowRecord;
    }

    @Override
    @Transactional
    public BorrowRecord getAndInit(Long uid, Boolean autoReplenishment) {
        BorrowRecord borrowRecord = this.getOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUid, uid)
                .eq(BorrowRecord::getPledgeStatus, PledgeStatus.PROCESS)
                .eq(BorrowRecord::isFinish, false));

        if (Objects.isNull(borrowRecord)) {
            borrowRecord = BorrowRecord.builder()
                    .uid(uid)
                    .autoReplenishment(autoReplenishment)
                    .pledgeStatus(PledgeStatus.PROCESS)
                    .build();
            this.save(borrowRecord);
        }
        return borrowRecord;
    }

    @Override
    public BorrowRecord getValid(Long uid) {
        BorrowRecord borrowRecord = this.getOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUid, uid)
                .eq(BorrowRecord::getPledgeStatus, PledgeStatus.PROCESS)
                .eq(BorrowRecord::isFinish, false));
        return Optional.ofNullable(borrowRecord).orElseThrow(ErrorCodeEnum.BORROW_RECORD_NOT_EXIST::generalException);
    }

    @Override
    public BorrowRecord get(Long uid) {
        return this.getOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUid, uid)
                .eq(BorrowRecord::isFinish, false));
    }

    @Override
    public IPage<MBorrowUserVO> pledgeUsers(Page<BorrowRecord> page, BorrowUserQuery query) {
        QueryWrapper<BorrowRecord> generate = QueryWrapperUtils.generate(BorrowRecord.class, query);
        return this.page(page, generate)
                .convert(record -> borrowConvert.toMBorrowUserVO(record));
    }

    @Override
    public void finish(Long uid, Long bid) {
        if (!borrowInterestService.payOff(uid, bid)
                || borrowRecordCoinService.payOff(uid, bid)
                || !borrowRecordPledgeService.releaseCompleted(uid, bid)) {
            throw ErrorCodeEnum.BORROW_RECORD_CANNOT_FINISH.generalException();
        }
        baseMapper.finish(bid, uid, LocalDateTime.now());
    }
}

