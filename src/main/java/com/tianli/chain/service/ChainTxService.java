package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.tianli.chain.entity.ChainTx;
import com.tianli.chain.mapper.ChainTxMapper;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.common.Constants;
import com.tianli.common.HttpUtils;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.common.log.LoggerHandle;
import com.tianli.mconfig.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * @author wangqiyun
 * @since 2020/11/16 21:02
 */

@Service
public class ChainTxService extends ServiceImpl<ChainTxMapper, ChainTx> {

    /**
     * 获取链上交易详情
     */
    private LineTransactionDetail getTransactionDetail(String txid) {
        LineTransactionDetail detail;
        Map<String, String> params = Maps.newHashMap();
        params.put("txid", txid);
        try {
            HttpResponse httpResponse = HttpUtils.doGet(Constants.TRANSACTION_DETAILS_HOST, Constants.TRANSACTION_DETAILS_PATH, "GET", Maps.newHashMap(), params);
            String dataJsonString = EntityUtils.toString(httpResponse.getEntity());
            detail = new Gson().fromJson(dataJsonString, LineTransactionDetail.class);
        } catch (Exception e) {
            return null;
        }
        return detail;
    }

    public Page<ChainTx> page(int page, int size, String address, String txid, String startTime, String endTime, ChargeStatus status) {
        LambdaQueryWrapper<ChainTx> lambdaQueryWrapper = new LambdaQueryWrapper<ChainTx>()
                .like(StringUtils.isNotBlank(address), ChainTx::getCollect_address, address)
                .like(StringUtils.isNotBlank(txid), ChainTx::getTxid, txid)
                .ge(StringUtils.isNotBlank(startTime), ChainTx::getComplete_time, startTime)
                .le(StringUtils.isNotBlank(endTime), ChainTx::getComplete_time, endTime)
                .eq(Objects.nonNull(status), ChainTx::getStatus, status)
                .orderByDesc(ChainTx::getId)
                ;
        return chainTxMapper.selectPage(new Page<>(page, size), lambdaQueryWrapper);
    }


    @Resource
    private LoggerHandle loggerHandle;
    @Resource
    private AsyncService asyncService;
    @Resource
    private ConfigService configService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private ChainTxMapper chainTxMapper;

}
