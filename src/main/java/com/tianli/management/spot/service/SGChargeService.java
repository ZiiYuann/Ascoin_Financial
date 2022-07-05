package com.tianli.management.spot.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.admin.AdminAndRoles;
import com.tianli.admin.AdminService;
import com.tianli.captcha.email.EmailSendFactory;
import com.tianli.captcha.email.enums.EmailSendEnum;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.TokenTransferService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.spot.dao.SGChargeMapper;
import com.tianli.management.spot.dto.SGWithdrawAuditDTO;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.vo.SGWithdrawListVo;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/15 2:56 下午
 */
@Service
public class SGChargeService extends ServiceImpl<SGChargeMapper, SGCharge> {
    @Resource
    SGChargeMapper sgChargeMapper;

    @Resource
    AdminService adminService;

    @Resource
    CurrencyTokenService currencyTokenService;

    @Resource
    EmailSendFactory emailSendFactory;

    @Resource
    UserService userService;

    public IPage<SGWithdrawListVo> selectPage(String username, String status, String startTime, String endTime, Integer page, Integer size) {
        Integer count = sgChargeMapper.withdrawCount(username, status, startTime, endTime);
        if (ObjectUtil.isNull(count) || count <= 0) {
            return new Page<>(page, size);
        }

        List<SGWithdrawListVo> sgWithdrawListVoList = sgChargeMapper.withdrawPage(username, status, startTime, endTime, (page - 1) * size, size);
        return new Page<SGWithdrawListVo>(page, size).setRecords(sgWithdrawListVoList).setTotal(Convert.toLong(count));
    }

    public BigDecimal sumAmount(String username, String status, String startTime, String endTime) {
        return sgChargeMapper.sumAmount(username, status, startTime, endTime);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(SGWithdrawAuditDTO sgWithdrawAuditDTO) {
        SGCharge sgCharge = this.getById(sgWithdrawAuditDTO.getId());
        if (ObjectUtil.isNull(sgCharge) || ObjectUtil.notEqual(sgCharge.getStatus(), ChargeStatus.created)) {
            return;
        }
        if (ObjectUtil.equal(sgWithdrawAuditDTO.getStatus(), ChargeStatus.chain_success)) {
            //审核成功
            currencyTokenService.reduce(sgCharge.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.getCurrencyCoinEnum(sgCharge.getToken()), sgCharge.getReal_amount(), sgCharge.getSn(), CurrencyLogDes.提现审核成功);
            currencyTokenService.reduce(sgCharge.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.getCurrencyCoinEnum(sgCharge.getToken()), sgCharge.getFee(), sgCharge.getSn(), CurrencyLogDes.提现手续费);
        } else if (ObjectUtil.equal(sgWithdrawAuditDTO.getStatus(), ChargeStatus.chaining)) {
            tokenTransferService.uploadChain(sgCharge);
        } else {
            //审核失败
            currencyTokenService.unfreeze(sgCharge.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.getCurrencyCoinEnum(sgCharge.getToken()), sgCharge.getAmount(), sgCharge.getSn(), CurrencyLogDes.提现审核失败);
        }
        update(sgWithdrawAuditDTO, sgCharge);
        if (ObjectUtil.equal(sgCharge.getStatus(),ChargeStatus.review_fail)){
            User user = userService._get(sgCharge.getUid());
            if (ObjectUtil.isNotNull(user)) {
                try {
                    emailSendFactory.send(EmailSendEnum.WITHDRAWAL_FAILED, null, sgCharge, user.getUsername());
                } catch (Exception e) {
                    log.error("提现失败邮件发送失败:", e);
                }
            }
        }
    }


    private void update(SGWithdrawAuditDTO sgWithdrawAuditDTO, SGCharge sgCharge) {
        AdminAndRoles my = adminService.my();
        sgCharge.setStatus(sgWithdrawAuditDTO.getStatus());
        sgCharge.setReason(sgWithdrawAuditDTO.getReason());
        sgCharge.setReason_en(sgWithdrawAuditDTO.getReason_en());
        sgCharge.setReview_note(sgWithdrawAuditDTO.getReview_note());
        sgCharge.setReviewer_id(my.getId());
        sgCharge.setReviewer(my.getUsername());
        sgCharge.setReviewer_time(LocalDateTime.now());
        sgCharge.setComplete_time(LocalDateTime.now());
        boolean updateResult = this.update(sgCharge, Wrappers.lambdaUpdate(SGCharge.class).eq(SGCharge::getId, sgCharge.getId()).eq(SGCharge::getStatus, ChargeStatus.created));
        if (!updateResult) ErrorCodeEnum.SYSTEM_BUSY.throwException();
    }

    @Resource
    private TokenTransferService tokenTransferService;
}
