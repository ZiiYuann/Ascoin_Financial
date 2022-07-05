package com.tianli.currency;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.ArtificialRecharge;
import com.tianli.currency.mapper.ArtificialRechargeMapper;
import com.tianli.currency.mapper.ArtificialRechargeType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.currency.ArtificialRechargeDTO;
import com.tianli.management.currency.ArtificialRechargePageDTO;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ArtificialRechargeService extends ServiceImpl<ArtificialRechargeMapper, ArtificialRecharge> {

    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private CurrencyLogService currencyLogService;

    @Resource
    private ArtificialRechargeMapper artificialRechargeMapper;

    @Transactional
    public void exchangeAmount(ArtificialRechargeDTO rechargeDTO) {
        User user = userService._get(rechargeDTO.getUid());
        if(Objects.isNull(user)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        long arId = CommonFunction.generalId();
        BigInteger amount = TokenCurrencyType.usdt_omni.amount(rechargeDTO.getAmount());
        String logSn = "ar_" + arId;
        ArtificialRechargeType type = rechargeDTO.getType();
        if(Objects.equals(type, ArtificialRechargeType.recharge)){
            currencyService.increase(user.getId(), CurrencyTypeEnum.normal, amount, logSn, CurrencyLogDes.线下充值.name());
        }else{
            currencyService.withdraw(user.getId(), CurrencyTypeEnum.normal, amount, logSn, CurrencyLogDes.线下提现.name());
        }
        UserInfo byId = userInfoService.getOrSaveById(rechargeDTO.getUid());
        AdminInfo adminInfo = AdminContent.get();
        LocalDateTime now = LocalDateTime.now();
        ArtificialRecharge build = ArtificialRecharge.builder()
                .id(arId)
                .create_time(now)
                .update_time(now)
                .uid(byId.getId())
                .username(byId.getUsername())
                .nick(byId.getNick())
                .type(type)
                .amount(amount)
                .voucher_image(rechargeDTO.getVoucher_image())
                .revoked(false)
                .recharge_admin_id(adminInfo.getAid())
                .recharge_admin_nick(adminInfo.getUsername())
                .remark(rechargeDTO.getRemark()).build();
        boolean save = super.save(build);
        if(!save){
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
    }

    /**
     * 撤回操作
     * 已经废除
     * @param id
     */
    public void revokeExchangeAmount(Long id) {
        CurrencyLog currencyLog = currencyLogService.getById(id);
        if(Objects.isNull(currencyLog)){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        String sn = currencyLog.getSn();
        String arId = sn.replace("ar_","");
        ArtificialRecharge recharge = super.getById(arId);
        User user = userService._get(recharge.getUid());
        if(Objects.isNull(user)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        String logSn = "ar_" + recharge.getId();
        currencyService.withdraw(user.getId(), CurrencyTypeEnum.normal, recharge.getAmount(), logSn, CurrencyLogDes.人工撤回.name());
        CurrencyLog log = currencyLogService.getOne(new LambdaQueryWrapper<CurrencyLog>().eq(CurrencyLog::getSn, logSn)
                .eq(CurrencyLog::getLog_type, CurrencyLogType.withdraw));
        UserInfo byId = userInfoService.getOrSaveById(recharge.getUid());
        AdminInfo adminInfo = AdminContent.get();
        ArtificialRecharge build = ArtificialRecharge.builder()
                .id(recharge.getId())
                .update_time(LocalDateTime.now())
                .log_id(recharge.getLog_id() + "," + log.getId())
                .uid(byId.getId())
                .username(byId.getUsername())
                .nick(byId.getNick())
                .revoked(true)
                .revoke_admin_id(adminInfo.getAid())
                .revoke_admin_nick(adminInfo.getUsername())
                .build();
        boolean update = super.updateById(build);
        if(!update){
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
    }

    public List<ArtificialRechargePageDTO> getPage(String username, String adminNick, String startTime, String endTime, Integer page, Integer size) {
        return artificialRechargeMapper.getPage(username, adminNick, startTime, endTime, Math.max(0,(page-1)*size), size);
    }

    public long getCount(String username, String adminNick, String startTime, String endTime) {
        return artificialRechargeMapper.getCount(username, adminNick, startTime, endTime);
    }

    public BigInteger getSumAmount(String username, String adminNick, String startTime, String endTime) {
        return artificialRechargeMapper.getSumAmount(username, adminNick, startTime, endTime);
    }
}