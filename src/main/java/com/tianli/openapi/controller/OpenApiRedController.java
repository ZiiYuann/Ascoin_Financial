package com.tianli.openapi.controller;

import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.accountred.vo.ORedEnvelopVO;
import com.tianli.accountred.vo.RedEnvelopeExchangeCodeVO;
import com.tianli.accountred.vo.RedEnvelopeExternGetDetailsVO;
import com.tianli.common.Constants;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.exception.Result;
import com.tianli.openapi.query.OpenapiRedQuery;
import com.tianli.rpc.RpcService;
import com.tianli.rpc.dto.UserInfoDTO;
import com.tianli.tool.IPUtils;
import com.tianli.tool.crypto.PBE;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-15
 **/
@RestController
@RequestMapping("/openapi/red")
public class OpenApiRedController {

    @Resource
    private RedEnvelopeService redEnvelopeService;
    @Resource
    private RedEnvelopeSpiltService redEnvelopeSpiltService;
    @Resource
    private RpcService rpcService;


    /**
     * 领取站外红包记录
     */
    @GetMapping("/extern/record")
    public Result<RedEnvelopeExternGetDetailsVO> externRedRecord(OpenapiRedQuery query
            , PageQuery<RedEnvelopeSpiltGetRecord> pageQuery) {

        String rid = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.parseLong(rid));

        var vo =
                redEnvelopeSpiltService.getExternDetailsRedis(redEnvelope, pageQuery);
        UserInfoDTO userInfoDTO = rpcService.userInfoDTO(redEnvelope.getUid());
        vo.setNickname(userInfoDTO.getNickname());
        return new Result<>(vo);
    }

    /**
     * 领取站外红包 垃圾需求逼我写垃圾代码
     */
    @GetMapping("/extern/get")
    public Result<RedEnvelopeExchangeCodeVO> externRedGet(@RequestHeader("fingerprint") String fingerprint
            , HttpServletRequest request, OpenapiRedQuery query) {
        String ip = IPUtils.getIpAddress(request);
        String id = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());

        String ipKey = RedisConstants.RED_ENVELOPE_LIMIT + ip;
        String fingerprintKey = RedisConstants.RED_ENVELOPE_LIMIT + fingerprint;

        RedEnvelopeExchangeCodeVO vo;
        if ((vo = redEnvelopeService.getExternCode(fingerprintKey)) != null
                || (vo = redEnvelopeService.getExternCode(ipKey)) != null) {
            RedEnvelopeSpilt redEnvelopeSpilt = redEnvelopeSpiltService.getById(vo.getSpiltRid());
            vo.setStatus(redEnvelopeSpilt.isReceive() ? RedEnvelopeStatus.EXCHANGE
                    : RedEnvelopeStatus.WAIT_EXCHANGE);
            return new Result<>(vo);
        }


        vo = redEnvelopeService.getExternCode(Long.parseLong(id), ipKey, fingerprintKey);
        return new Result<>(vo);
    }

    /**
     * 没必要的接口，前端不愿意加缓存非要后端加
     */
    @GetMapping("/extern")
    public Result<RedEnvelopeExchangeCodeVO> externRedGet(@RequestHeader("fingerprint") String fingerprint
            , HttpServletRequest request) {
        String ip = IPUtils.getIpAddress(request);

        String ipKey = RedisConstants.RED_ENVELOPE_LIMIT + ip;
        String fingerprintKey = RedisConstants.RED_ENVELOPE_LIMIT + fingerprint;

        RedEnvelopeExchangeCodeVO vo;
        if ((vo = redEnvelopeService.getExternCode(fingerprintKey)) != null
                || (vo = redEnvelopeService.getExternCode(ipKey)) != null) {
            RedEnvelopeSpilt redEnvelopeSpilt = redEnvelopeSpiltService.getById(vo.getSpiltRid());
            vo.setStatus(redEnvelopeSpilt.isReceive() ? RedEnvelopeStatus.EXCHANGE
                    : RedEnvelopeStatus.WAIT_EXCHANGE);
            return new Result<>(vo);
        }

        return new Result<>(vo);
    }

    /**
     * 站外红包信息
     */
    @GetMapping("/extern/info")
    public Result<ORedEnvelopVO> externRedGet(OpenapiRedQuery query) {
        String rid = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.parseLong(rid));
        UserInfoDTO userInfoDTO = rpcService.userInfoDTO(redEnvelope.getUid());
        ORedEnvelopVO vo = ORedEnvelopVO.builder()
                .coin(redEnvelope.getCoin())
                .nickname(userInfoDTO.getNickname())
                .remarks(redEnvelope.getRemarks())
                .build();
        return new Result<>(vo);
    }
}
