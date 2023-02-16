package com.tianli.management.controller;

import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.service.RedEnvelopeConfigService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.other.query.RedEnvelopeConfigIoUQuery;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.admin.AdminContent;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-16
 **/
@RestController
@RequestMapping("/management/red")
public class ManagerRedEnvelopeController {

    @Resource
    RedEnvelopeConfigService redEnvelopeConfigService;


    /**
     * 新增或更新红包配置信息
     *
     * @param redEnvelopeConfigQuery
     * @return
     */
    @RequestMapping("/save")
    @AdminPrivilege
    public Result save(@RequestBody @Valid RedEnvelopeConfigIoUQuery redEnvelopeConfigQuery) {
        String nickname = AdminContent.get().getNickname();
        RedEnvelopeChannel channel = redEnvelopeConfigQuery.getChannel();
        //站外红包配置最大数默认1000，聊天红包无配置
        if (channel.equals(RedEnvelopeChannel.EXTERN)) {
            if (redEnvelopeConfigQuery.getNum() > 1000 || redEnvelopeConfigQuery.getNum() < 0) {
                return Result.fail(ErrorCodeEnum.RED_NUM_CONFIAG_ERROR);
            }
        }
        BigDecimal minAmount = redEnvelopeConfigQuery.getMinAmount();
        BigDecimal limitAmount = new BigDecimal("0.000001");
        if (limitAmount.compareTo(minAmount) > 0) {
            return Result.fail(ErrorCodeEnum.RED_LIMIT_AMOUNT);
        }
        redEnvelopeConfigService.saveOrUpdate(nickname, redEnvelopeConfigQuery);
        return Result.success();
    }
}
