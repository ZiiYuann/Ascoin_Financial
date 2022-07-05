package com.tianli.management.agentmanage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.Lists;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.charge.ChargeService;
import com.tianli.common.CommonFunction;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.mapper.Currency;
import com.tianli.deposit.ChargeDepositService;
import com.tianli.deposit.mapper.ChargeDeposit;
import com.tianli.deposit.mapper.ChargeDepositStatus;
import com.tianli.deposit.mapper.ChargeDepositType;
import com.tianli.deposit.mapper.DepositSettlementType;
import com.tianli.dividends.settlement.ChargeSettlementService;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import com.tianli.dividends.settlement.mapper.ChargeSettlementType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.agentmanage.controller.DepositAuditDTO;
import com.tianli.management.agentmanage.controller.SaveBalanceAccountDTO;
import com.tianli.management.agentmanage.controller.SettlementAuditDTO;
import com.tianli.management.agentmanage.controller.UpdateBalanceAccountDTO;
import com.tianli.management.agentmanage.mapper.AgentManageMapper;
import com.tianli.management.agentmanage.mapper.AgentManagePageDTO;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserIdentity;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tianli.charge.ChargeService.ONE_HUNDRED;
import static com.tianli.charge.ChargeService.TEN_BILLION;

@Service
public class AgentManageService {

    @Resource
    private AgentManageMapper agentManageMapper;

    @Resource
    private AgentService agentService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private UserService userService;

    @Resource
    private AddressService addressService;

    @Resource
    private ChargeDepositService chargeDepositService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private ChargeSettlementService chargeSettlementService;

    public List<AgentManagePageDTO> getPage(String nick, String phone, String startTime, String endTime, Integer page, Integer size) {

        return agentManageMapper.getPage(nick, phone, startTime, endTime, Math.max((page - 1) * size, 0), size);
    }

    public double getSumDeposit(String nick, String username, String startTime, String endTime, Boolean focus) {
        Map<String, BigDecimal> sumCurrency = agentManageMapper.selectSumCurrency(CurrencyTypeEnum.deposit, nick, username, startTime, endTime, focus);
        return TokenCurrencyType.usdt_omni.money(sumCurrency.get("sumBalance").toBigInteger());
    }

    public Map<String, BigDecimal> getSumUnSettlement(String nick, String username, String startTime, String endTime, Boolean focus) {
        return agentManageMapper.selectSumCurrency(CurrencyTypeEnum.settlement, nick, username, startTime, endTime, focus);
    }

    public Map<String,BigDecimal> getSumBet(List<Long> betIdList, String phone, BetResultEnum result, String startTime, String endTime) {
        if(CollectionUtils.isEmpty(betIdList)){
            Map<String,BigDecimal> map = new HashMap<>();
            map.put("sumBetAmount",BigDecimal.ZERO);
            map.put("sumDividendsAmount",BigDecimal.ZERO);
            map.put("sumRebateAmount",BigDecimal.ZERO);
            map.put("sumRebateAmountBF",BigDecimal.ZERO);
            return map;
        }
        List<String> idStringList = betIdList.stream().map(String::valueOf).collect(Collectors.toList());
        String inSqlString = String.join(",", idStringList);
        return agentManageMapper.selectSumBet(inSqlString, phone, result, startTime, endTime);
    }

    @Transactional
    public void deleteAgent(Long id) {
        // 1. 删除代理商
        boolean removeAgent = agentService.removeById(id);
        if(!removeAgent) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        // 2 恢复普通身份
        boolean restoreNormalIdentity = userService.updateIdentityById(UserIdentity.normal, id);
        if(!restoreNormalIdentity) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        // 3. 关闭押金和分红余额数据
        boolean removeCurrency = currencyService.remove(new LambdaQueryWrapper<Currency>().eq(Currency::getUid, id).in(Currency::getType, Lists.newArrayList(CurrencyTypeEnum.deposit, CurrencyTypeEnum.settlement)));
        if(!removeCurrency) ErrorCodeEnum.SYSTEM_BUSY.throwException();

        // 4. 关闭保证金和分红地址
        boolean removeAddress = addressService.remove(new LambdaQueryWrapper<Address>().eq(Address::getUid, id).in(Address::getType, Lists.newArrayList(CurrencyTypeEnum.deposit, CurrencyTypeEnum.settlement)));
        if(!removeAddress) ErrorCodeEnum.SYSTEM_BUSY.throwException();
    }

    @Transactional
    public void saveAgent(Agent agent) {
        // 1. 增加代理商
        if(!agentService.save(agent)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        // 2 更新身份为代理商
        boolean updateIdentity = userService.updateIdentityById(UserIdentity.senior_agent, agent.getId());
        if(!updateIdentity){
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        // 3. 开通押金和分红余额数据
        if (!currencyService.save(Currency.builder().id(CommonFunction.generalId()).uid(agent.getId()).type(CurrencyTypeEnum.deposit).build())) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        if (!currencyService.save(Currency.builder().id(CommonFunction.generalId()).uid(agent.getId()).type(CurrencyTypeEnum.settlement).build())) ErrorCodeEnum.SYSTEM_ERROR.throwException();

        // 4. 生成代理商充值地址
//        addressService.get(agent.getId(), CurrencyTypeEnum.deposit);
        try {
            addressService.get_(agent.getId(), CurrencyTypeEnum.settlement);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void updateBalanceAccount(Long uid, UpdateBalanceAccountDTO dto) {
        ChargeDeposit chargeDeposit = chargeDepositService.getById(dto.getId());
        if(Objects.isNull(chargeDeposit) || !Objects.equals(chargeDeposit.getUid(), uid)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        if(!Objects.equals(chargeDeposit.getSettlement_type(), DepositSettlementType.balance)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        ChargeSettlement chargeSettlement = chargeSettlementService.getOne(new LambdaQueryWrapper<ChargeSettlement>().eq(ChargeSettlement::getSn, chargeDeposit.getSn().replaceAll("CD", "CS")));
        if(Objects.isNull(chargeSettlement)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        User user = userService._get(uid);
        if(Objects.isNull(user)){
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        if(Objects.isNull(userInfo)){
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        Agent agent = agentService.getById(uid);
        if(Objects.isNull(agent)){
            ErrorCodeEnum.ACCESS_DENY.throwException();
        }
        currencyService.increase(uid, CurrencyTypeEnum.deposit, chargeDeposit.getAmount(), chargeDeposit.getSn(), CurrencyLogDes.平账归还.name());
        currencyService.withdrawPresumptuous(uid, CurrencyTypeEnum.settlement, chargeDeposit.getAmount(), chargeDeposit.getSn(), CurrencyLogDes.结算归还.name());
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.deposit);
        if(Objects.isNull(currency)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        BigInteger amount;
        if(currency.getRemain().compareTo(amount = TokenCurrencyType.usdt_omni.amount(dto.getAmount())) < 0){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        LocalDateTime now = LocalDateTime.now();
        ChargeDeposit deposit = ChargeDeposit.builder()
                .id(dto.getId())
                .complete_time(now)
                .uid(uid)
                .uid_username(agent.getUsername())
                .uid_nick(agent.getNick())
                .amount(amount)
                .fee(BigInteger.ZERO)
                .real_amount(amount)
                .note(dto.getNote())
                .build();
        boolean update = chargeDepositService.updateById(deposit);
        if(!update){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        currencyService.withdraw(uid, CurrencyTypeEnum.deposit, amount, chargeDeposit.getSn(), CurrencyLogDes.平账.name());
        currencyService.increase(uid, CurrencyTypeEnum.settlement, amount, chargeDeposit.getSn(), CurrencyLogDes.结算.name());
        ChargeSettlement updateChargeSettlement = ChargeSettlement.builder()
                .id(chargeSettlement.getId())
                .complete_time(now)
                .uid(uid)
                .uid_username(agent.getUsername())
                .uid_nick(agent.getNick())
                .amount(amount)
                .fee(BigInteger.ZERO)
                .real_amount(amount)
                .note(dto.getNote())
                .build();
        boolean updateCS = chargeSettlementService.updateById(updateChargeSettlement);
        if(!updateCS){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
    }

    @Transactional
    public void delBalanceAccount(Long uid, Long id) {
        ChargeDeposit chargeDeposit = chargeDepositService.getById(id);
        if(Objects.isNull(chargeDeposit) || !Objects.equals(chargeDeposit.getUid(), uid)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        if(!Objects.equals(chargeDeposit.getSettlement_type(), DepositSettlementType.balance)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        ChargeSettlement chargeSettlement = chargeSettlementService.getOne(new LambdaQueryWrapper<ChargeSettlement>().eq(ChargeSettlement::getSn, chargeDeposit.getSn().replaceAll("CD", "CS")));
        if(Objects.isNull(chargeSettlement)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        Agent agent = agentService.getById(uid);
        if(Objects.isNull(agent)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        currencyService.increase(uid, CurrencyTypeEnum.deposit, chargeDeposit.getAmount(), chargeDeposit.getSn(), CurrencyLogDes.平账归还.name());
        currencyService.withdrawPresumptuous(uid, CurrencyTypeEnum.settlement, chargeDeposit.getAmount(), chargeDeposit.getSn(), CurrencyLogDes.结算归还.name());
        boolean removeCD = chargeDepositService.removeById(id);
        if(!removeCD){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        boolean removeCS = chargeSettlementService.removeById(chargeSettlement.getId());
        if(!removeCS){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
    }

    @Transactional
    public void depositAudit(DepositAuditDTO dto) {
        ChargeDeposit chargeDeposit = chargeDepositService.getById(dto.getId());
        if(Objects.isNull(chargeDeposit)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if(!Objects.equals(ChargeDepositStatus.created,chargeDeposit.getStatus())){
            ErrorCodeEnum.throwException("订单已被审核");
        }
        if(ChargeDepositStatus.review_fail.equals(dto.getStatus())){
            boolean update = chargeDepositService.update(new LambdaUpdateWrapper<ChargeDeposit>()
                    .set(ChargeDeposit::getStatus, dto.getStatus())
                    .set(ChargeDeposit::getReview_note, dto.getReview_note())
                    .set(ChargeDeposit::getComplete_time, LocalDateTime.now())
                    .eq(ChargeDeposit::getId, dto.getId())
                    .eq(ChargeDeposit::getStatus, ChargeDepositStatus.created));
            if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
            if(TokenCurrencyType.usdt_omni.equals(chargeDeposit.getCurrency_type())){
                currencyService.unfreeze(chargeDeposit.getUid(), CurrencyTypeEnum.deposit, chargeDeposit.getAmount(), String.format("chargeDeposit%s", chargeDeposit.getSn()), CurrencyLogDes.撤回.name());
            } else if (TokenCurrencyType.usdt_erc20.equals(chargeDeposit.getCurrency_type())) {
                currencyService.unfreeze(chargeDeposit.getUid(), CurrencyTypeEnum.deposit, chargeDeposit.getAmount().multiply(new BigInteger("100")), String.format("chargeDeposit%s", chargeDeposit.getSn()), CurrencyLogDes.撤回.name());
            }
            return;
        }
        boolean update = chargeDepositService.update(new LambdaUpdateWrapper<ChargeDeposit>()
                .set(ChargeDeposit::getStatus, dto.getStatus())
                .set(ChargeDeposit::getReview_note, dto.getReview_note())
                .eq(ChargeDeposit::getId, dto.getId())
                .eq(ChargeDeposit::getStatus, ChargeDepositStatus.created));
        if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        chargeService.uploadChain(chargeDeposit.getCurrency_type(), chargeDeposit.getReal_amount(), chargeDeposit.getSn(),
                chargeDeposit.getFrom_address(), chargeDeposit.getTo_address());
    }

    @Transactional
    public void settlementAudit(SettlementAuditDTO dto) {
        ChargeSettlement chargeSettlement = chargeSettlementService.getById(dto.getId());
        if(Objects.isNull(chargeSettlement)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if(!ChargeSettlementStatus.created.equals(chargeSettlement.getStatus())){
            ErrorCodeEnum.throwException("订单已被审核");
        }
        if(ChargeSettlementStatus.review_fail.equals(dto.getStatus())){
            boolean update = chargeSettlementService.update(new LambdaUpdateWrapper<ChargeSettlement>()
                    .set(ChargeSettlement::getStatus, dto.getStatus())
                    .set(ChargeSettlement::getReview_note, dto.getReview_note())
                    .set(ChargeSettlement::getComplete_time, LocalDateTime.now())
                    .eq(ChargeSettlement::getId, dto.getId())
                    .eq(ChargeSettlement::getStatus, ChargeSettlementStatus.created));
            if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
            if(TokenCurrencyType.usdt_bep20.equals(chargeSettlement.getCurrency_type())){
                currencyService.unfreeze(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, chargeSettlement.getAmount().divide(ChargeService.TEN_BILLION), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现.name());
            } else if (TokenCurrencyType.usdt_erc20.equals(chargeSettlement.getCurrency_type())
                    || TokenCurrencyType.usdt_trc20.equals(chargeSettlement.getCurrency_type())) {
                currencyService.unfreeze(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, chargeSettlement.getAmount().multiply(ChargeService.ONE_HUNDRED), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现.name());
            } else if(TokenCurrencyType.BF_bep20.equals(chargeSettlement.getCurrency_type())) {
                currencyService.unfreeze(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, CurrencyTokenEnum.BF_bep20, chargeSettlement.getAmount(), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现.name());
            }
            return;
        }
        boolean update = chargeSettlementService.update(new LambdaUpdateWrapper<ChargeSettlement>()
                .set(ChargeSettlement::getStatus, dto.getStatus())
                .set(ChargeSettlement::getReview_note, dto.getReview_note())
                .eq(ChargeSettlement::getId, dto.getId())
                .eq(ChargeSettlement::getStatus, ChargeSettlementStatus.created));
        if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        chargeService.uploadChain2(chargeSettlement);
    }

    @Transactional
    public void settlementAudit2(SettlementAuditDTO dto) {
        ChargeSettlement chargeSettlement = chargeSettlementService.getById(dto.getId());
        if(Objects.isNull(chargeSettlement)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if(!ChargeSettlementStatus.created.equals(chargeSettlement.getStatus())){
            ErrorCodeEnum.throwException("订单已被审核");
        }
        if(ChargeSettlementStatus.review_fail.equals(dto.getStatus())){
            boolean update = chargeSettlementService.update(new LambdaUpdateWrapper<ChargeSettlement>()
                    .set(ChargeSettlement::getStatus, dto.getStatus())
                    .set(ChargeSettlement::getReview_note, dto.getReview_note())
                    .set(ChargeSettlement::getComplete_time, LocalDateTime.now())
                    .eq(ChargeSettlement::getId, dto.getId())
                    .eq(ChargeSettlement::getStatus, ChargeSettlementStatus.created));
            if(!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
            if(TokenCurrencyType.usdt_bep20.equals(chargeSettlement.getCurrency_type())){
                currencyService.unfreeze(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, chargeSettlement.getAmount().divide(ChargeService.TEN_BILLION), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现.name());
            } else if (TokenCurrencyType.usdt_erc20.equals(chargeSettlement.getCurrency_type())
                    || TokenCurrencyType.usdt_trc20.equals(chargeSettlement.getCurrency_type())) {
                currencyService.unfreeze(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, chargeSettlement.getAmount().multiply(ChargeService.ONE_HUNDRED), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现.name());
            } else if(TokenCurrencyType.BF_bep20.equals(chargeSettlement.getCurrency_type())) {
                currencyService.unfreeze(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, CurrencyTokenEnum.BF_bep20, chargeSettlement.getAmount(), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现.name());
            }
        } else if (ChargeSettlementStatus.transaction_success.equals(dto.getStatus())) {
            boolean update = chargeSettlementService.update(new LambdaUpdateWrapper<ChargeSettlement>()
                    .set(ChargeSettlement::getStatus, dto.getStatus())
                    .set(ChargeSettlement::getReview_note, dto.getReview_note())
                    .eq(ChargeSettlement::getId, dto.getId())
                    .eq(ChargeSettlement::getStatus, ChargeSettlementStatus.created));
            if (!update) ErrorCodeEnum.SYSTEM_BUSY.throwException();
            if(TokenCurrencyType.usdt_bep20.equals(chargeSettlement.getCurrency_type())){
                currencyService.reduce(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, chargeSettlement.getReal_amount().divide(TEN_BILLION), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.结算.name());
                currencyService.reduce(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, chargeSettlement.getFee().divide(TEN_BILLION), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现手续费.name());
            } else if (TokenCurrencyType.usdt_erc20.equals(chargeSettlement.getCurrency_type())
                    || TokenCurrencyType.usdt_trc20.equals(chargeSettlement.getCurrency_type())) {
                currencyService.reduce(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, chargeSettlement.getReal_amount().multiply(ONE_HUNDRED), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.结算.name());
                currencyService.reduce(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, chargeSettlement.getFee().multiply(ONE_HUNDRED), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现手续费.name());
            } else if(TokenCurrencyType.BF_bep20.equals(chargeSettlement.getCurrency_type())) {
                currencyService.reduce(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, CurrencyTokenEnum.BF_bep20, chargeSettlement.getReal_amount(), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.结算.name());
                currencyService.reduce(chargeSettlement.getUid(), CurrencyTypeEnum.settlement, CurrencyTokenEnum.BF_bep20, chargeSettlement.getFee(), String.format("charge_%s", chargeSettlement.getSn()), CurrencyLogDes.提现手续费.name());
            }
        } else ErrorCodeEnum.SYSTEM_BUSY.throwException();
    }

    @Transactional
    public void saveBalanceAccount(Long uid, SaveBalanceAccountDTO dto, BigInteger amount, String nick, String username) {

        LocalDateTime now = LocalDateTime.now();
        long generalId = CommonFunction.generalId();
        String sn = CommonFunction.generalSn(generalId);
        ChargeDeposit deposit = ChargeDeposit.builder()
                .id(generalId)
                .create_time(now)
                .complete_time(now)
                .status(ChargeDepositStatus.transaction_success)
                .uid(uid)
                .uid_username(username)
                .uid_nick(nick)
                .sn("CD" + sn)
                .currency_type(TokenCurrencyType.usdt_omni)
                .charge_type(ChargeDepositType.withdraw)
                .settlement_type(DepositSettlementType.balance)
                .amount(amount)
                .fee(BigInteger.ZERO)
                .real_amount(amount)
                .note(dto.getNote())
                .build();
        boolean save = chargeDepositService.save(deposit);
        if(!save){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        currencyService.withdraw(uid, CurrencyTypeEnum.deposit, amount, deposit.getSn(), CurrencyLogDes.平账.name());
        long csGenerateId = CommonFunction.generalId();
        ChargeSettlement chargeSettlement = ChargeSettlement.builder()
                .id(csGenerateId)
                .create_time(now)
                .complete_time(now)
                .status(ChargeSettlementStatus.transaction_success)
                .uid(uid)
                .uid_username(username)
                .uid_nick(nick)
                .sn("CS" + sn)
                .currency_type(TokenCurrencyType.usdt_omni)
                .charge_type(ChargeSettlementType.recharge)
                .settlement_type(DepositSettlementType.balance)
                .amount(amount)
                .fee(BigInteger.ZERO)
                .real_amount(amount)
                .note(dto.getNote())
                .build();
        currencyService.increase(uid, CurrencyTypeEnum.settlement, amount, deposit.getSn(), CurrencyLogDes.结算.name());
        boolean saveCS = chargeSettlementService.save(chargeSettlement);
        if(!saveCS){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
    }
}
