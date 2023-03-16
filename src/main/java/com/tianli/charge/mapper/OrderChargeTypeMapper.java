package com.tianli.charge.mapper;

import com.tianli.account.vo.OrderChargeTypeVO;
import com.tianli.charge.entity.OrderChargeType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yangkang
 * @since 2023-03-10
 */
@Mapper
public interface OrderChargeTypeMapper extends BaseMapper<OrderChargeType> {

//    List<OrderChargeTypeVO> listChargeTypeByGroup();
}
