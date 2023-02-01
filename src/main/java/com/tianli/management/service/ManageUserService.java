package com.tianli.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.address.mapper.Address;
import com.tianli.product.financial.service.FinancialService;
import com.tianli.product.fund.service.IFundRecordService;
import com.tianli.product.fund.vo.FundMainPageVO;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.vo.FinancialUserInfoVO;
import com.tianli.management.vo.MUserListVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-30
 **/
@Service
public class ManageUserService {

    @Resource
    private FinancialService financialService;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private IFundRecordService fundRecordService;

    /**
     * 理财用户信息y
     */
    public IPage<MUserListVO> financialUserPage(String queryUid, IPage<Address> page) {
        IPage<FinancialUserInfoVO> financialUserInfoVOIPage = financialService.financialUserPage(queryUid, page);

        return financialUserInfoVOIPage.convert(financialUserInfoVO -> {
            MUserListVO mUserListVO = managementConverter.toManagerUserListVO(financialUserInfoVO);
            Long uid = financialUserInfoVO.getUid();

            FundMainPageVO fundMainPageVO = fundRecordService.mainPage(uid);
            mUserListVO.setWaitIncomeAmount(fundMainPageVO.getWaitPayInterestAmount());
            mUserListVO.setCalIncomeAmount(fundMainPageVO.getPayInterestAmount().add(fundMainPageVO.getWaitPayInterestAmount()));
            return mUserListVO;
        });
    }

}
