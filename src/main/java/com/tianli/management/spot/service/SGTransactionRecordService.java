package com.tianli.management.spot.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.management.spot.dao.SGTransactionRecordMapper;
import com.tianli.management.spot.vo.SGTransactionRecordListVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/16 11:10 上午
 */
@Service
public class SGTransactionRecordService {

    @Resource
    SGTransactionRecordMapper sgTransactionRecordMapper;


    public IPage<SGTransactionRecordListVo> selectPage(String username, String token, String startTime, String endTime, Integer page, Integer size) {
        Long count = sgTransactionRecordMapper.selectCount(username, token, startTime, endTime);
        if (ObjectUtil.isNull(count) || count <= 0L) {
            return new Page<>(page, size);
        }
        List<SGTransactionRecordListVo> transactionRecordListVos = sgTransactionRecordMapper.selectList(username, token, startTime, endTime, (page - 1) * size, size);
        return new Page<SGTransactionRecordListVo>(page, size).setRecords(transactionRecordListVos).setTotal(count);
    }
}
