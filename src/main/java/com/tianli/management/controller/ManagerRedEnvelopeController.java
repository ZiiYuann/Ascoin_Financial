package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.service.RedEnvelopeConfigService;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.other.query.RedEnvelopeConfigIoUQuery;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.admin.AdminContent;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

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
    @Resource
    private RedEnvelopeService redEnvelopeService;

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
        if (Objects.nonNull(query.getLimitAmount()) && Objects.nonNull(query.getNum())
          &&RedEnvelopeChannel.EXTERN.equals(query.getChannel())) {
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
    public Result details(@RequestParam("coin") String coin,
                          @RequestParam("channel") RedEnvelopeChannel channel) {
        RedEnvelopeConfig one = redEnvelopeConfigService.getDetails(coin, channel);
        return Result.success(one);
    }

    /**
     * 站外红包详情
     */
    @GetMapping("/records")
    public Result<IPage<RedEnvelope>> records(@RequestParam("status") RedEnvelopeStatus status, PageQuery<RedEnvelope> pageQuery) {
        LambdaQueryWrapper<RedEnvelope> queryWrapper = new LambdaQueryWrapper<>();
        Optional.ofNullable(status).ifPresent(s -> queryWrapper.eq(RedEnvelope::getStatus, status));
        return Result.success(redEnvelopeService.page(pageQuery.page(), queryWrapper));
    }
}

