package com.tianli.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.VisibleTypeEnum;
import com.tianli.account.vo.OrderChargeTypeVO;
import com.tianli.charge.entity.OrderChargeType;
import com.tianli.charge.mapper.OrderChargeTypeMapper;
import com.tianli.charge.service.IOrderChargeTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.management.service.IWalletAgentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yangkang
 * @since 2023-03-10
 */
@Service
public class OrderChargeTypeServiceImpl extends ServiceImpl<OrderChargeTypeMapper, OrderChargeType> implements IOrderChargeTypeService {

    @Resource
    IWalletAgentService walletAgentService;

    @Override
    public List<OrderChargeTypeVO> listChargeType(Long uid) {
       boolean isAgent = walletAgentService.isAgent(uid);
        LambdaQueryWrapper<OrderChargeType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderChargeType::getIsEnable,1);
        if (!isAgent) {
            wrapper.eq(OrderChargeType::getVisibleType, VisibleTypeEnum.normal.getSymbol());
        }
        List<OrderChargeType> list = this.list(wrapper);
        Map<String, List<OrderChargeType>> groupByTypes = list.stream().collect(Collectors.groupingBy(OrderChargeType::getOperationGroup));
        List<OrderChargeTypeVO> orderChargeTypeVOS = new ArrayList<>();
        for (Map.Entry<String, List<OrderChargeType>> entry : groupByTypes.entrySet()) {
            OrderChargeTypeVO orderChargeTypeVO = new OrderChargeTypeVO();
            orderChargeTypeVO.setGroupEn(entry.getKey());
            orderChargeTypeVO.setGroup(ChargeTypeGroupEnum.getTypeGroup(entry.getKey()));
            orderChargeTypeVO.setOrderChargeTypes(entry.getValue());
            orderChargeTypeVO.setOrder(ChargeTypeGroupEnum.getOrder(entry.getKey()));
            orderChargeTypeVOS.add(orderChargeTypeVO);
        }
        orderChargeTypeVOS = orderChargeTypeVOS.stream().sorted(Comparator.comparing(OrderChargeTypeVO::getOrder)).collect(Collectors.toList());
        return orderChargeTypeVOS;
    }

    @Override
    public List<OrderChargeTypeVO> chargeTypeList() {
        List<OrderChargeType> list = this.list();
        Map<String, List<OrderChargeType>> groupByTypes = list.stream().collect(Collectors.groupingBy(OrderChargeType::getOperationGroup));
        List<OrderChargeTypeVO> orderChargeTypeVOS = new ArrayList<>();
        for (Map.Entry<String, List<OrderChargeType>> entry : groupByTypes.entrySet()) {
            OrderChargeTypeVO orderChargeTypeVO = new OrderChargeTypeVO();
            orderChargeTypeVO.setGroup(entry.getKey());
            orderChargeTypeVO.setOperationGroupName(ChargeTypeGroupEnum.getTypeGroup(entry.getKey()));
            orderChargeTypeVO.setOrderChargeTypes(entry.getValue());
            orderChargeTypeVOS.add(orderChargeTypeVO);
        }
        return orderChargeTypeVOS;
    }
}
