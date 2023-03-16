package com.tianli.charge.service;

import com.tianli.account.vo.OrderChargeTypeVO;
import com.tianli.charge.entity.OrderChargeType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xianeng
 * @since 2023-03-10
 */
public interface IOrderChargeTypeService extends IService<OrderChargeType> {

    /**
     * 获取新的交易类型列表 2023-03-10需求变动
     * @param uid
     */
    List<OrderChargeTypeVO> listChargeType(Long uid);

    /**
     * 获取所有的交易类型
     * @return
     */
    List<OrderChargeTypeVO> chargeTypeList();
}
