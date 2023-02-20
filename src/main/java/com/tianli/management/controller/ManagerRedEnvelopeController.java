package com.tianli.management.controller;

import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.service.RedEnvelopeConfigService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.other.query.RedEnvelopeConfigIoUQuery;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.admin.AdminContent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Objects;

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
     * @param query
     * @return
     */
    @RequestMapping("/save")
    @AdminPrivilege
    public Result save(@RequestBody @Valid RedEnvelopeConfigIoUQuery query) {
        String nickname = AdminContent.get().getNickname();
        if (Objects.nonNull(query.getLimitAmount()) && Objects.nonNull(query.getNum())) {
            BigDecimal divide = query.getLimitAmount().divide(new BigDecimal(query.getNum()));
            if (divide.compareTo(query.getMinAmount()) < 0) {
                return Result.fail("编辑不合规");
            }
        }
        redEnvelopeConfigService.saveOrUpdate(nickname, query);
        return Result.success();
    }


    /**
     * 站外红包详情
     *
     * @param coin 币种
     * @return
     */
    @GetMapping("/details")
    public Result details(@RequestParam("coin") String coin) {
        RedEnvelopeConfig one = redEnvelopeConfigService.getDetails(coin, RedEnvelopeChannel.EXTERN);
        return Result.success(one);
    }
}

