package com.tianli.management.fundmanagement;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tianli.admin.AdminAndRoles;
import com.tianli.admin.AdminService;
import com.tianli.charge.ChargeService;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.tianli.charge.ChargeService.ONE_HUNDRED;
import static com.tianli.charge.ChargeService.TEN_BILLION;

/**
 * @author chensong
 * @date 2021-01-08 17:11
 * @since 1.0.0
 */
@Service
public class WithdrawalManageService {
    @Resource
    private ChargeService chargeService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    AdminService adminService;

    @Transactional
    public void audit(ChargeAuditDTO dto) {
        Charge charge = chargeService.getById(dto.getId());
        if(Objects.isNull(charge)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if(!Objects.equals(charge.getStatus(), ChargeStatus.created)){
            ErrorCodeEnum.throwException("订单已被审核");
        }
        AdminAndRoles my = adminService.my();
        // 审核失败 修改订单状态 并解冻余额
        if(ChargeStatus.review_fail.equals(dto.getStatus())){
            boolean update = chargeService.update(new LambdaUpdateWrapper<Charge>()
                    .set(Charge::getStatus, dto.getStatus())
                    .set(Charge::getReview_note, dto.getReview_note())
                    .set(Charge::getComplete_time, LocalDateTime.now())
                    .set(Charge::getReviewer_time,LocalDateTime.now())
                    .set(Charge::getReviewer,my.getUsername())
                    .set(Charge::getReviewer_id,my.getId())
                    .set(Charge::getReason,dto.getReason())
                    .set(Charge::getReason_en,dto.getReason_en())
                    .eq(Charge::getId, dto.getId())
                    .eq(Charge::getStatus, ChargeStatus.created));
            if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
            if(TokenCurrencyType.usdt_bep20.equals(charge.getCurrency_type()) || TokenCurrencyType.usdc_bep20.equals(charge.getCurrency_type())){
                currencyService.unfreeze(charge.getUid(), CurrencyTypeEnum.normal, charge.getAmount().divide(new BigInteger("10000000000")), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核失败.name());
            } else if (TokenCurrencyType.usdt_erc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdt_trc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdc_erc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdc_trc20.equals(charge.getCurrency_type())
            ) {
                currencyService.unfreeze(charge.getUid(), CurrencyTypeEnum.normal, charge.getAmount().multiply(new BigInteger("100")), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核失败.name());
            } else if(TokenCurrencyType.BF_bep20.equals(charge.getCurrency_type())) {
                currencyService.unfreeze(charge.getUid(), CurrencyTypeEnum.normal, CurrencyTokenEnum.BF_bep20, charge.getAmount(), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核失败.name());
            }
        } else if (ChargeStatus.chaining.equals(dto.getStatus())){
            //审核成功 将状态修改为 上链中
            boolean update = chargeService.update(new LambdaUpdateWrapper<Charge>()
                    .set(Charge::getStatus, dto.getStatus())
                    .set(Charge::getReview_note, dto.getReview_note())
                    .set(Charge::getReviewer_time,LocalDateTime.now())
                    .set(Charge::getReviewer,my.getUsername())
                    .set(Charge::getReviewer_id,my.getId())
                    .set(Charge::getReason,dto.getReason())
                    .set(Charge::getReason_en,dto.getReason_en())
                    .eq(Charge::getId, dto.getId())
                    .eq(Charge::getStatus, ChargeStatus.created));
            if (!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
            //将交易提交到链上
            chargeService.uploadChain2(charge);
        } else ErrorCodeEnum.SYSTEM_BUSY.throwException();
    }

    @Transactional
    public void audit2(ChargeAuditDTO dto) {
        Charge charge = chargeService.getById(dto.getId());
        if(Objects.isNull(charge)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if(!Objects.equals(charge.getStatus(), ChargeStatus.created)){
            ErrorCodeEnum.throwException("订单已被审核");
        }
        AdminAndRoles my = adminService.my();
        // 审核失败
        if(ChargeStatus.review_fail.equals(dto.getStatus())){
            boolean update = chargeService.update(new LambdaUpdateWrapper<Charge>()
                    .set(Charge::getStatus, dto.getStatus())
                    .set(Charge::getReview_note, dto.getReview_note())
                    .set(Charge::getComplete_time, LocalDateTime.now())
                    .set(Charge::getReviewer_time,LocalDateTime.now())
                    .set(Charge::getReviewer,my.getUsername())
                    .set(Charge::getReviewer_id,my.getId())
                    .set(Charge::getReason,dto.getReason())
                    .set(Charge::getReason_en,dto.getReason_en())
                    .eq(Charge::getId, dto.getId())
                    .eq(Charge::getStatus, ChargeStatus.created));
            if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
            //返回冻结金额
            if(TokenCurrencyType.usdt_bep20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdc_bep20.equals(charge.getCurrency_type())
            ){
                currencyService.unfreeze(charge.getUid(), CurrencyTypeEnum.normal, charge.getAmount().divide(new BigInteger("10000000000")), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核失败.name());
            } else if (TokenCurrencyType.usdt_erc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdt_trc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdc_erc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdc_trc20.equals(charge.getCurrency_type())
            ) {
                currencyService.unfreeze(charge.getUid(), CurrencyTypeEnum.normal, charge.getAmount().multiply(new BigInteger("100")), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核失败.name());
            } else if(TokenCurrencyType.BF_bep20.equals(charge.getCurrency_type())) {
                currencyService.unfreeze(charge.getUid(), CurrencyTypeEnum.normal, CurrencyTokenEnum.BF_bep20, charge.getAmount(), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核失败.name());
            }
        } else if (ChargeStatus.chain_success.equals(dto.getStatus())){
            //审核成功 手动转账
            boolean update = chargeService.update(new LambdaUpdateWrapper<Charge>()
                    .set(Charge::getStatus, dto.getStatus())
                    .set(Charge::getReview_note, dto.getReview_note())
                    .set(Charge::getReviewer_time,LocalDateTime.now())
                    .set(Charge::getReviewer,my.getUsername())
                    .set(Charge::getReviewer_id,my.getId())
                    .set(Charge::getReason,dto.getReason())
                    .set(Charge::getReason_en,dto.getReason_en())
                    .eq(Charge::getId, dto.getId())
                    .eq(Charge::getStatus, ChargeStatus.created));
            if (!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
            //根据不同链的usdt 调节后面的0的个数后 扣除余额
            if(TokenCurrencyType.usdt_bep20.equals(charge.getCurrency_type()) || TokenCurrencyType.usdc_bep20.equals(charge.getCurrency_type())){
                currencyService.reduce(charge.getUid(), CurrencyTypeEnum.normal, charge.getReal_amount().divide(TEN_BILLION), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核成功.name());
                currencyService.reduce(charge.getUid(), CurrencyTypeEnum.normal, charge.getFee().divide(TEN_BILLION), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现手续费.name());
            } else if (TokenCurrencyType.usdt_erc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdt_trc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdc_erc20.equals(charge.getCurrency_type())
                    || TokenCurrencyType.usdc_trc20.equals(charge.getCurrency_type())
            ) {
                currencyService.reduce(charge.getUid(), CurrencyTypeEnum.normal, charge.getReal_amount().multiply(ONE_HUNDRED), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核成功.name());
                currencyService.reduce(charge.getUid(), CurrencyTypeEnum.normal, charge.getFee().multiply(ONE_HUNDRED), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现手续费.name());
            } else if(TokenCurrencyType.BF_bep20.equals(charge.getCurrency_type())) {
                currencyService.reduce(charge.getUid(), CurrencyTypeEnum.normal, CurrencyTokenEnum.BF_bep20, charge.getReal_amount(), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现审核成功.name());
                currencyService.reduce(charge.getUid(), CurrencyTypeEnum.normal, CurrencyTokenEnum.BF_bep20, charge.getFee(), String.format("charge_%s", charge.getSn()), CurrencyLogDes.提现手续费.name());
            }
        } else ErrorCodeEnum.SYSTEM_BUSY.throwException();
    }
}
