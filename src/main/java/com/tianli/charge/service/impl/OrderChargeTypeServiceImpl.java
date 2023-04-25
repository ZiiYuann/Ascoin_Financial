package com.tianli.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.vo.OrderChargeTypeVO;
import com.tianli.charge.entity.OrderChargeType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.OperationTypeEnum;
import com.tianli.charge.enums.VisibleTypeEnum;
import com.tianli.charge.mapper.OrderChargeTypeMapper;
import com.tianli.charge.service.OrderChargeTypeService;
import com.tianli.management.service.IWalletAgentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
public class OrderChargeTypeServiceImpl extends ServiceImpl<OrderChargeTypeMapper, OrderChargeType>
        implements OrderChargeTypeService {

    private static final Map<ChargeTypeGroupEnum, List<ChargeType>> chargeTypesByLevel3Group;

    @Resource
    IWalletAgentService walletAgentService;

    static {
        ArrayList<ChargeType> chargeTypes = new ArrayList<>(List.of(ChargeType.values()));
        chargeTypesByLevel3Group = chargeTypes.stream().filter(type -> Objects.nonNull(type.getLevel3Group()))
                .collect(Collectors.groupingBy(ChargeType::getLevel3Group));
    }

    @Override
    public List<OrderChargeTypeVO> listChargeType(Long uid) {
        boolean isAgent = walletAgentService.isAgent(uid);
        LambdaQueryWrapper<OrderChargeType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderChargeType::getIsEnable, 1);
        if (!isAgent) {
            wrapper.eq(OrderChargeType::getVisibleType, VisibleTypeEnum.normal.getSymbol());
        }
        List<OrderChargeType> list = this.list(wrapper);
        var groupByTypes = list.stream().filter(orderChargeType ->
                        !orderChargeType.getType().equals(ChargeType.assure_withdraw)
                        && !orderChargeType.getType().equals(ChargeType.assure_recharge))
                .collect(Collectors.groupingBy(OrderChargeType::getOperationGroup));
        List<OrderChargeTypeVO> orderChargeTypeVOS = new ArrayList<>();
        for (Map.Entry<ChargeTypeGroupEnum, List<OrderChargeType>> entry : groupByTypes.entrySet()) {
            OrderChargeTypeVO orderChargeTypeVO = new OrderChargeTypeVO();
            orderChargeTypeVO.setOrderChargeTypes(entry.getValue());
            orderChargeTypeVO.setChargeTypeGroup(entry.getKey());
            orderChargeTypeVOS.add(orderChargeTypeVO);
        }
        orderChargeTypeVOS = orderChargeTypeVOS.stream().sorted(Comparator.comparing(OrderChargeTypeVO::getOrder)).collect(Collectors.toList());
        return orderChargeTypeVOS;
    }

    @Override
    public List<OrderChargeTypeVO> orderChargeTypeVOs() {
        List<OrderChargeType> list = this.list();
        var groupByTypes = list.stream()
                .collect(Collectors.groupingBy(OrderChargeType::getOperationGroup));
        List<OrderChargeTypeVO> orderChargeTypeVOS = new ArrayList<>();

        for (Map.Entry<ChargeTypeGroupEnum, List<OrderChargeType>> entry : groupByTypes.entrySet()) {
            ChargeTypeGroupEnum group = entry.getKey();
            OrderChargeTypeVO orderChargeTypeVO = new OrderChargeTypeVO();
            orderChargeTypeVO.setChargeTypeGroup(group);
            orderChargeTypeVO.setOrderChargeTypes(entry.getValue());
            orderChargeTypeVOS.add(orderChargeTypeVO);
        }
        return orderChargeTypeVOS;
    }

    @Override
    public List<ChargeType> chargeTypes(ChargeTypeGroupEnum chargeTypeGroup) {
        return chargeTypesByLevel3Group.get(chargeTypeGroup);
    }


    @Override
    public List<ChargeType> chargeTypes(OperationTypeEnum operationType) {
        return this.baseMapper.chargeTypes(operationType);
    }

    @Override
    public List<OperationTypeEnum> operationTypes(ChargeTypeGroupEnum chargeTypeGroup) {
        return this.baseMapper.operationTypes(chargeTypeGroup);
    }
}
