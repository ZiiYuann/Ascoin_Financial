package com.tianli.management.recycle.record;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lzy
 * @date 2022/4/26 15:55
 */
@Service
public class RecycleRecordService extends ServiceImpl<RecycleRecordMapper, RecycleRecord> {
    public List<RecycleRecord> queryNotProcess() {
        return this.list(Wrappers.lambdaQuery(RecycleRecord.class)
                .eq(RecycleRecord::getProcess, Boolean.FALSE)
                .eq(RecycleRecord::getMain_currency, Boolean.TRUE));
    }
}
