package com.tianli.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MainWalletLogMapper extends BaseMapper<MainWalletLog> {

    Map<String, Object> sumAmount(@Param("address") String address,
                                  @Param("txid") String txid,
                                  @Param("chain_type") String chain_type,
                                  @Param("start") String start,
                                  @Param("end") String end);
}
