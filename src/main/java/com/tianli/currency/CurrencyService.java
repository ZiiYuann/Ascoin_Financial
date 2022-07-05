package com.tianli.currency;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.CurrencyMapper;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户余额表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class CurrencyService extends ServiceImpl<CurrencyMapper, Currency> {

    @Resource
    private CurrencyMapper currencyMapper;
    @Resource
    private CurrencyLogService currencyLogService;
    @Resource
    private AsyncService asyncService;

    public Currency get(long uid, CurrencyTypeEnum type) {
        Currency currency = _get(uid, type);
        if (currency == null) {
            currency = Currency.builder()
                    .id(CommonFunction.generalId())
                    .uid(uid)
                    .type(type)
                    .balance(BigInteger.ZERO)
                    .freeze(BigInteger.ZERO)
                    .remain(BigInteger.ZERO)
                    .build();
            final Currency currencyFinal = currency;
            asyncService.async(() -> {
                currencyMapper.insert(currencyFinal);
            });
//            currencyMapper.insert(currency);
        }
        return currency;
    }

    public Currency _get(long uid, CurrencyTypeEnum type) {
        return currencyMapper.get(uid, type);
    }

    public List<Currency> list(long uid) {
        return currencyMapper.list(uid);
    }

    public List<Currency> listByIds(List<Long> uids) {
        List<String> idStringList = uids.stream().filter(Objects::nonNull).map(e -> e.toString()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idStringList)) {
            return Lists.newArrayList();
        }
        return currencyMapper.listByIds(String.join(",", idStringList));
    }

    public List<Currency> listByIds(List<Long> uids, CurrencyTypeEnum type) {
        List<String> idStringList = uids.stream().filter(Objects::nonNull).map(e -> e.toString()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idStringList)) {
            return Lists.newArrayList();
        }
        return currencyMapper.listByIdsAndType(String.join(",", idStringList), type);
    }

    /**
     * 增加金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void increase(long uid, CurrencyTypeEnum type, BigInteger amount, String sn, String des) {
        if (!_increase(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des))
            ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    @Transactional
    public void increase(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        if (!_increase(uid, type, token, amount, sn, des)) ErrorCodeEnum.CREDIT_LACK.throwException();
    }


    /**
     * 扣除冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void reduce(long uid, CurrencyTypeEnum type, BigInteger amount, String sn, String des) {
        if (!_reduce(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des))
            ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    @Transactional
    public void reduce(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        if (!_reduce(uid, type, token, amount, sn, des)) ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    /**
     * 扣除可用金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void withdraw(long uid, CurrencyTypeEnum type, BigInteger amount, String sn, String des) {
        if (!_withdraw(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des))
            ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    @Transactional
    public void withdraw(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        if (!_withdraw(uid, type, token, amount, sn, des)) ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    /**
     * 扣除可用金额
     * 可为负的情况
     * [暂为分红结算余额专用接口]
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void withdrawPresumptuous(long uid, CurrencyTypeEnum type, BigInteger amount, String sn, String des) {
        if (!_withdrawPresumptuous(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des))
            ErrorCodeEnum.CREDIT_LACK.throwException();
    }


    @Transactional
    public void withdrawPresumptuous(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        if (!_withdrawPresumptuous(uid, type, token, amount, sn, des)) ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    /**
     * 冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void freeze(long uid, CurrencyTypeEnum type, BigInteger amount, String sn, String des) {
        if (!_freeze(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des))
            ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    @Transactional
    public void freeze(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        if (!_freeze(uid, type, token, amount, sn, des)) ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    /**
     * 解冻金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void unfreeze(long uid, CurrencyTypeEnum type, BigInteger amount, String sn, String des) {
        if (!_unfreeze(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des))
            ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    @Transactional
    public void unfreeze(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        if (!_unfreeze(uid, type, token, amount, sn, des)) ErrorCodeEnum.CREDIT_LACK.throwException();
    }

    private boolean _increase(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        get(uid, type);
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long result = currencyMapper.increase(uid, type, amount);
            Currency currency = get(uid, type);
            currencyLogService.add(uid, type, token, CurrencyLogType.increase, amount, sn, des, currency.getBalance(), currency.getFreeze(), currency.getRemain());
            return result > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long result = currencyMapper.increaseBF(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.increase, amount, sn, des, currency.getBalance_BF(), currency.getFreeze_BF(), currency.getRemain_BF());
            return result > 0L;
        }
        return false;
    }

    private boolean _reduce(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        get(uid, type);
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long result = currencyMapper.reduce(uid, type, amount);
            Currency currency = get(uid, type);
            currencyLogService.add(uid, type, token, CurrencyLogType.reduce, amount, sn, des, currency.getBalance(), currency.getFreeze(), currency.getRemain());
            return result > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long result = currencyMapper.reduceBF(uid, type, amount);
            Currency currency = get(uid, type);
            currencyLogService.add(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.reduce, amount, sn, des, currency.getBalance_BF(), currency.getFreeze_BF(), currency.getRemain_BF());
            return result > 0L;
        }
        return false;
    }

    private boolean _withdraw(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        get(uid, type);
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long result = currencyMapper.withdraw(uid, type, amount);
            Currency currency = get(uid, type);
            currencyLogService.add(uid, type, token, CurrencyLogType.withdraw, amount, sn, des, currency.getBalance(), currency.getFreeze(), currency.getRemain());
            return result > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long result = currencyMapper.withdrawBF(uid, type, amount);
            Currency currency = get(uid, type);
            currencyLogService.add(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.withdraw, amount, sn, des, currency.getBalance_BF(), currency.getFreeze_BF(), currency.getRemain_BF());
            return result > 0L;
        }
        return false;
    }

    private boolean _withdrawPresumptuous(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token,
                                          BigInteger amount, String sn, String des) {
        get(uid, type);
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long result = currencyMapper.withdrawPresumptuous(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, token, CurrencyLogType.withdraw, amount, sn, des, currency.getBalance(), currency.getFreeze(), currency.getRemain());
            return result > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long result = currencyMapper.withdrawPresumptuousBF(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.withdraw, amount, sn, des, currency.getBalance_BF(), currency.getFreeze_BF(), currency.getRemain_BF());
            return result > 0L;
        }
        return false;
    }

    private boolean _freeze(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        get(uid, type);
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long result = currencyMapper.freeze(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, token, CurrencyLogType.freeze, amount, sn, des, currency.getBalance(), currency.getFreeze(), currency.getRemain());
            return result > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long result = currencyMapper.freezeBF(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.freeze, amount, sn, des, currency.getBalance_BF(), currency.getFreeze_BF(), currency.getRemain_BF());
            return result > 0L;
        }
        return false;
    }

    private boolean _unfreeze(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        get(uid, type);
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long result = currencyMapper.unfreeze(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, token, CurrencyLogType.unfreeze, amount, sn, des, currency.getBalance(), currency.getFreeze(), currency.getRemain());
            return result > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long result = currencyMapper.unfreezeBF(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.unfreeze, amount, sn, des, currency.getBalance_BF(), currency.getFreeze_BF(), currency.getRemain_BF());
            return result > 0L;
        }
        return false;
    }


    public void lowWithdraw(long uid, CurrencyTypeEnum type, BigInteger amount, String sn, String des) {
        lowWithdraw(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des);
    }

    public void lowWithdraw(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)) {
            long result = currencyMapper.lowWithdraw(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, CurrencyLogType.withdraw, amount, sn, des, currency.getBalance(), currency.getFreeze(), currency.getRemain());
            if (result <= 0L) {
                ErrorCodeEnum.CREDIT_LACK.throwException();
            }
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long result = currencyMapper.lowWithdrawBF(uid, type, amount);
            Currency currency = currencyMapper.get(uid, type);
            currencyLogService.add(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.withdraw, amount, sn, des, currency.getBalance_BF(), currency.getFreeze_BF(), currency.getRemain_BF());
            if (result <= 0L) {
                ErrorCodeEnum.CREDIT_LACK.throwException();
            }
        }
    }

    public void transfer(long uid, CurrencyTypeEnum from, CurrencyTypeEnum to, BigInteger amount, String sn) {
        long result_from = currencyMapper.decrease(uid, from, amount);
        long result_to = currencyMapper.increase(uid, to, amount);
        Currency currency_from = currencyMapper.get(uid, from);
        Currency currency_to = currencyMapper.get(uid, to);
        currencyLogService.add(uid, from, CurrencyLogType.reduce, amount, sn, CurrencyLogDes.划出.name(), currency_from.getBalance(), currency_from.getFreeze(), currency_from.getRemain());
        currencyLogService.add(uid, to, CurrencyLogType.increase, amount, sn, CurrencyLogDes.划入.name(), currency_to.getBalance(), currency_to.getFreeze(), currency_to.getRemain());
        if (result_from <= 0L || result_to <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }

    public List<Long> listAgentFocus() {
        return currencyMapper.listAgentFocus();
    }

    public BigInteger betIncrease(Long uid, CurrencyTypeEnum normal, CurrencyTokenEnum usdt_omni, BigInteger returnAmount, BigInteger ordinaryAmount, String sn, String dsc) {
        if (ordinaryAmount.compareTo(BigInteger.ZERO) <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger returnOrdinaryAmount;
        if (ordinaryAmount.compareTo(returnAmount) >= 0) {
            returnOrdinaryAmount = returnAmount;
            this.increase(uid, normal, usdt_omni, returnAmount, sn, dsc);
        } else {
            returnOrdinaryAmount = ordinaryAmount;
            this.increase(uid, normal, usdt_omni, ordinaryAmount, sn, dsc);
        }
        return returnOrdinaryAmount;
    }
}
