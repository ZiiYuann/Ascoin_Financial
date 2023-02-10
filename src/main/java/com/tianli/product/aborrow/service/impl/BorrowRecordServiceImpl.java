package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.product.aborrow.entity.BorrowRecord;
import com.tianli.product.aborrow.mapper.BorrowRecordMapper;
import com.tianli.product.aborrow.service.BorrowRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord>
        implements BorrowRecordService {

    @Override
    @Transactional
    public BorrowRecord getAndInit(Long uid, Boolean autoReplenishment) {
        BorrowRecord borrowRecord = this.getOne(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUid, uid)
                .eq(BorrowRecord::isFinish, false));

        if (Objects.isNull(borrowRecord)) {
            borrowRecord = BorrowRecord.builder()
                    .uid(uid)
                    .autoReplenishment(autoReplenishment)
                    .build();
            this.save(borrowRecord);
        }
        return borrowRecord;
    }
}

