package com.tianli.user.referral;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.user.referral.mapper.UserReferral;
import com.tianli.user.referral.mapper.UserReferralMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-03
 */
@Service
public class UserReferralService extends ServiceImpl<UserReferralMapper, UserReferral>{

    /**
     * 获取用户推荐链
     */
    public LinkedList<Long> userReferralChain(Long uid){
        LinkedList<Long> uidList = new LinkedList<>();
        if(Objects.isNull(uid)){
            return uidList;
        }
        while (true){
            UserReferral byId = super.getById(uid);
            if (Objects.isNull(byId)){
                uidList.addLast(uid);
                break;
            }
            uidList.addLast(uid);
            uid = byId.getReferral_id();
        }
        return uidList;
    }

}
