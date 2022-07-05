package com.tianli.management.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.DiscountCurrencyService;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.user.controller.CustomerReferralVO;
import com.tianli.management.user.controller.CustomerUpdateDTO;
import com.tianli.management.user.mapper.CustomerDTO;
import com.tianli.management.user.mapper.CustomerManageMapper;
import com.tianli.rebate.RebateService;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.robot.RobotCouponService;
import com.tianli.robot.RobotOrderService;
import com.tianli.robot.mapper.RobotCoupon;
import com.tianli.robot.mapper.RobotOrder;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.tool.MapTool;
import com.tianli.user.UserService;
import com.tianli.user.dto.RobotUpdateDTO;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.logs.mapper.UserIpLog;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomerManageService {
    @Resource
    private CustomerManageMapper customerManageMapper;

    @Resource
    private RebateService rebateService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private UserService userService;

    @Resource
    private UserIpLogService userIpLogService;

    @Resource
    private CurrencyLogService currencyLogService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private DiscountCurrencyService discountCurrencyService;

    @Resource
    private UserReferralService userReferralService;

    public List<CustomerDTO> page(String phone, UserStatus status, Integer user_type, String startTime, String endTime, Integer page, Integer size,String queryUserIds){
        return customerManageMapper.selectPage(phone, status, user_type, startTime, endTime, page, size,queryUserIds);
    }

    public int count(String phone, UserStatus status, Integer user_type, String startTime, String endTime,String queryUserIds) {
        return customerManageMapper.selectCount(phone, status, user_type, startTime, endTime,queryUserIds);
    }

    public List<Rebate> rebatePage(Long id, Integer page, Integer size) {
        Page<Rebate> rebatePage = rebateService.page(new Page<>(page, size), new LambdaQueryWrapper<Rebate>().eq(Rebate::getRebate_uid, id).orderByDesc(Rebate::getId));
        return rebatePage.getRecords();
    }

    public int rebateCount(Long id) {
        return rebateService.count(new LambdaQueryWrapper<Rebate>().eq(Rebate::getRebate_uid, id));
    }

    public int withdrawalCount(Long id) {
        return chargeService.selectCount(id, null, ChargeType.withdraw, null, null, null, null);
    }

    public List<Charge> withdrawalPage(Long id, Integer page, Integer size) {
        return chargeService.selectPage(id, null, ChargeType.withdraw, null, null, null, null, page, size);
    }

    public void userStatusNegate(Long id) {
        userService.negateStatus(id);
    }

    public int referralCount(Long id) {
        return userReferralService.count(new LambdaQueryWrapper<UserReferral>().eq(UserReferral::getReferral_id, id));
    }

    public List<CustomerReferralVO> referralPage(Long id, Integer page, Integer size) {
        Page<UserReferral> userReferralPage = userReferralService.page(new Page<>(page, size), new LambdaQueryWrapper<UserReferral>().eq(UserReferral::getReferral_id, id).orderByDesc(UserReferral::getCreate_time));
        List<UserReferral> records = userReferralPage.getRecords();
        List<Long> ids = records.stream().map(UserReferral::getId).collect(Collectors.toList());
        List<User> byIds = userService.getByIds(ids);
        List<CustomerReferralVO> vos = byIds.stream().map(e -> CustomerReferralVO.trans(e,id)).collect(Collectors.toList());

        List<UserIpLog> list = userIpLogService.list(Wrappers.lambdaQuery(UserIpLog.class)
                .eq(UserIpLog::getBehavior, GrcCheckModular.邀请绑定).in(UserIpLog::getBehavior_id, ids));
        if (!CollectionUtils.isEmpty(list)){
            Map<Long, UserIpLog> collectMap = list.stream().collect(Collectors.toMap(UserIpLog::getId, Function.identity()));
            vos.forEach(e -> {
                UserIpLog userIpLog = collectMap.get(e.getId());
                if(Objects.nonNull(userIpLog)){
                    e.setGrc_score(userIpLog.getGrc_score());
                }
            });
        }
        return vos;
    }

    public int currencyCount(Long id) {
        return currencyLogService.count(new LambdaQueryWrapper<CurrencyLog>().eq(CurrencyLog::getUid, id)
                .eq(CurrencyLog::getType, CurrencyTypeEnum.normal)
                .in(CurrencyLog::getLog_type, Lists.newArrayList(CurrencyLogType.increase, CurrencyLogType.reduce, CurrencyLogType.withdraw)));
    }

    public List<CurrencyLog> currencyPage(Long id, Integer page, Integer size) {
        Page<CurrencyLog> currencyLogPage = currencyLogService.page(new Page<>(page, size), new LambdaQueryWrapper<CurrencyLog>().eq(CurrencyLog::getUid, id)
                .eq(CurrencyLog::getType, CurrencyTypeEnum.normal)
                .in(CurrencyLog::getLog_type, Lists.newArrayList(CurrencyLogType.increase, CurrencyLogType.reduce, CurrencyLogType.withdraw))
                .orderByDesc(CurrencyLog::getId));
        return currencyLogPage.getRecords();
    }

    public BigInteger sumBalance(String phone, UserStatus status,Integer user_type, String startTime, String endTime,String queryUserIds) {
        return customerManageMapper.selectSumBalance(phone, status,user_type, startTime, endTime,queryUserIds);
    }

    public BigDecimal newSumBalance(String phone, UserStatus status, Integer user_type, String startTime, String endTime, String queryUserIds) {
        return customerManageMapper.newSumBalance(phone, status,user_type, startTime, endTime,queryUserIds);
    }

    public BigInteger sumBalanceBF(String phone, UserStatus status,Integer user_type, String startTime, String endTime,String queryUserIds) {
        return customerManageMapper.selectSumBalanceBF(phone, status,user_type, startTime, endTime,queryUserIds);
    }

    public void updateRobot(Long id, CustomerUpdateDTO dto) {
        userService.updateRobot(id, RobotUpdateDTO.convert(dto));
    }

    public void updateNode(Long id, String node) {
        userService.updateNode(id, StringUtils.isBlank(node) ? "" : node);
    }

    public void updateCreditScore(Long id,Integer credit_score,String adjust_reason) {
        userService.updateCreditScore(id, credit_score,StringUtils.isBlank(adjust_reason) ? "" : adjust_reason);
    }



    @Resource
    private RobotOrderService robotOrderService;

    @Resource
    private RobotCouponService robotCouponService;

    public MapTool getRobotById(Long id) {
        User user = userService._get(id);
        if(Objects.isNull(user)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        RobotOrder robotOrder = robotOrderService.getOne(Wrappers.lambdaQuery(RobotOrder.class).eq(RobotOrder::getUid, id));
        if(Objects.isNull(robotOrder)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        RobotCoupon robotCoupon = robotCouponService.getById(robotOrder.getRobot_code());
        return MapTool.Map()
                .put("auto_count", robotOrder.getCount())
                .put("auto_amount", robotOrder.getAmount())
                .put("interval_time", robotCoupon.getInterval_time())
                .put("win_rate", robotCoupon.getWin_rate())
                .put("profit_rate", robotCoupon.getProfit_rate());
    }

    public void updateUserType(Long id, Integer user_type) {
        userService.updateUserType(id, user_type);
    }

    public Currency getCurrencyById(Long id, CurrencyTypeEnum financial) {
        return currencyService.get(id, financial);
    }

    public DiscountCurrency getDiscountCurrencyById(Long id) {
        return discountCurrencyService.getById(id);
    }

}
