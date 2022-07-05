package com.tianli.management.kyc;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.currency.DiscountCurrencyService;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.kyc.KycService;
import com.tianli.kyc.mapper.Kyc;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mgt/kyc")
public class KycMgtController {

    @Resource
    private KycService kycService;

    @Resource
    DiscountCurrencyService discountCurrencyService;

    @Resource
    ConfigService configService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.KYC管理)
    public Result page(KycMgtPageReq req) {
        Page<Kyc> page = kycService.page(new Page<>(req.getPage(), req.getSize()),
                Wrappers.lambdaQuery(Kyc.class)
                        .like(StringUtils.isNotBlank(req.getUsername()), Kyc::getUsername, req.getUsername())
                        .like(StringUtils.isNotBlank(req.getCertificate_no()), Kyc::getCertificate_no, req.getCertificate_no())
                        .like(StringUtils.isNotBlank(req.getCertificate_type()), Kyc::getCertificate_type, req.getCertificate_type())
                        .like(StringUtils.isNotBlank(req.getReal_name()), Kyc::getReal_name, req.getReal_name())
                        .eq(Objects.nonNull(req.getStatus()), Kyc::getStatus, req.getStatus()));
        List<Kyc> records = page.getRecords();
        List<KycPageVO> vos = records.stream().map(KycPageVO::convert).collect(Collectors.toList());
        return Result.instance().setData(MapTool.Map()
                .put("total", page.getTotal())
                .put("list", vos));
    }

    @GetMapping("/info/{id}")
    @AdminPrivilege(and = {Privilege.KYC管理, Privilege.KYC详情})
    public Result info(@PathVariable("id") Long id) {
        Kyc kyc = kycService.getById(id);
        if (Objects.isNull(kyc)) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        return Result.success(KycPageInfoVO.convert(kyc));
    }

    @PostMapping("/audit")
    @AdminPrivilege(and = {Privilege.KYC管理})
    @Transactional(rollbackFor = Exception.class)
    public Result audit(@RequestBody @Valid KycAuditReq req) {
        AdminInfo adminInfo = AdminContent.get();
        Kyc kyc = kycService.getById(req.getId());
        if (Objects.isNull(kyc)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        if (Objects.equals(req.getStatus(), 1)) {
            DiscountCurrency discountCurrency = discountCurrencyService.getById(kyc.getUid());
            String amount = configService._get(ConfigConstants.KYC_AWARD);
            if (Objects.isNull(discountCurrency) || !discountCurrency.getKyc_certification()) {
                if (StringUtils.isNotBlank(amount) && Convert.toBigInteger(amount).compareTo(BigInteger.ZERO) > 0) {
                    discountCurrencyService.KYC(kyc.getUid(), TokenCurrencyType.usdt_omni.amount(amount));
                    discountCurrencyService.KYCCertification(kyc.getUid());
                }
            }
        }
        kycService.update(Wrappers.lambdaUpdate(Kyc.class)
                .set(Kyc::getOpt_admin, adminInfo.getUsername())
                .set(Kyc::getOpt_admin_id, adminInfo.getAid())
                .set(Kyc::getOpt_time, LocalDateTime.now())
                .set(Kyc::getStatus, req.getStatus())
                .set(Kyc::getStatus, req.getStatus())
                .set(Kyc::getNode, req.getNode())
                .set(Kyc::getEn_node, req.getEn_node())
                .eq(Kyc::getId, req.getId()));
        return Result.success();
    }

}
