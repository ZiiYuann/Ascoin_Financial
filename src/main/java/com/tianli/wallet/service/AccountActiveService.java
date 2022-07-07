package com.tianli.wallet.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.account.query.AccountActiveQuery;
import com.tianli.wallet.covert.WalletCovert;
import com.tianli.wallet.enums.AccountActiveStatus;
import com.tianli.wallet.mapper.AccountActiveMapper;
import com.tianli.wallet.vo.WalletActiveVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-06
 **/
@Service
public class AccountActiveService extends ServiceImpl<AccountActiveMapper, AccountActive> {

    @Resource
    private WalletCovert walletCovert;
    @Resource
    private RequestInitService requestInitService;

    /**
     * 激活钱包
     */
    public void activateWallet(AccountActiveQuery query) {
        //TODO 校验密码是否正确
        Long uid = requestInitService.get().getUid();
        AccountActive walletActive = getBaseMapper().selectByUid(uid);
        Optional<AccountActive> walletActiveOptional = Optional.ofNullable(walletActive);
        if(walletActiveOptional.isPresent()){
            // 校验激活状态
            return;
        }
        walletActive = this.init();
        this.getBaseMapper().insert(walletActive);
    }

    /**
     * 查询用户钱包激活状态
     */
    public WalletActiveVo getWalletActiveVo(){
        Long uid = requestInitService.get().getUid();
        AccountActive walletActive = Optional.ofNullable(getBaseMapper().selectByUid(uid)).orElse(AccountActive.builder().build());
        return walletCovert.toVo(walletActive);
    }

    /**
     * 初始化用户钱包激活信息
     */
    private AccountActive init() {
        Long uid = requestInitService.get().getUid();
        LocalDateTime now = requestInitService.now();
        return AccountActive.builder()
                .id(CommonFunction.generalId()).uid(uid)
                .createTime(now).updateTime(now)
                .status(AccountActiveStatus.ACTIVE.getType()).build();
    }
}










