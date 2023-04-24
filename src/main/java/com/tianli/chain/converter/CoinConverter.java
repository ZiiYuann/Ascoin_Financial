package com.tianli.chain.converter;

import com.tianli.chain.entity.CoinBase;
import com.tianli.management.vo.CoinBaseVO;
import org.mapstruct.Mapper;

/**
 * @autoor xianeng
 * @data 2023/4/24 10:16
 */
@Mapper(componentModel = "spring")
public interface CoinConverter {

    CoinBaseVO toCoinBaseVo(CoinBase coinBase);
}
