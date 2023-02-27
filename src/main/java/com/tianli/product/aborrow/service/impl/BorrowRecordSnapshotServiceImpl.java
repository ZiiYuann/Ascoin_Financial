package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.product.aborrow.entity.BorrowRecordSnapshot;
import com.tianli.product.aborrow.mapper.BorrowRecordSnapshotMapper;
import com.tianli.product.aborrow.service.BorrowRecordSnapshotService;
import org.springframework.stereotype.Service;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Service
public class BorrowRecordSnapshotServiceImpl extends ServiceImpl<BorrowRecordSnapshotMapper, BorrowRecordSnapshot>
        implements BorrowRecordSnapshotService {
}
