package com.tianli.openapi.controller;

import com.tianli.accountred.dto.RedEnvelopStatusDTO;
import com.tianli.accountred.entity.RedEnvelope;
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
import com.tianli.mconfig.ConfigService;
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
import java.util.Locale;

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
    @Resource
    private ConfigService configService;


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
        String id = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());
        String ip = IPUtils.getIpAddress(request);
        RedEnvelopStatusDTO redEnvelopStatusDTO;
        // EXCHANGE WAIT_EXCHANGE
        if ((redEnvelopStatusDTO = redEnvelopeSpiltService.getIpOrFingerDTO(fingerprint, Long.valueOf(id))) != null) {
            return new Result<>(redEnvelopStatusDTO);
        }

        return new Result<>(redEnvelopeSpiltService.getExchangeCode(Long.parseLong(id), ip, fingerprint));
    }

    /**
     * 没必要的接口，前端不愿意加缓存非要后端加
     */
    @GetMapping("/extern")
    public Result<RedEnvelopeExchangeCodeVO> extern(@RequestHeader("fingerprint") String fingerprint
            , OpenapiRedQuery query) {
        String rid = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());
        RedEnvelopeExchangeCodeVO vo;

        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.valueOf(rid));
        if (!RedEnvelopeStatus.valid(redEnvelope.getStatus())) {
            // FINISH OVERDUE
            vo = new RedEnvelopeExchangeCodeVO(redEnvelope.getStatus());
            return new Result<>(vo);
        }

        RedEnvelopStatusDTO redEnvelopStatusDTO;
        // EXCHANGE WAIT_EXCHANGE
        if ((redEnvelopStatusDTO = redEnvelopeSpiltService.getIpOrFingerDTO(fingerprint, redEnvelope.getId())) != null) {
            return new Result<>(redEnvelopStatusDTO);
        }

        // PROCESS
        String externKey = RedisConstants.RED_EXTERN + rid;
        long now = System.currentTimeMillis();
        if ((redEnvelopStatusDTO = redEnvelopeSpiltService.getNotExpireDTO(externKey, now)) != null) {
            return new Result<>(redEnvelopStatusDTO);
        }

        // FINISH_TEMP
        redEnvelopStatusDTO = redEnvelopeSpiltService.getLatestExpireDTO(externKey, now);
        return new Result<>(redEnvelopStatusDTO);
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

    @GetMapping("/redPackage")
    public String redPackage(OpenapiRedQuery query) {
        return this.openRedPackage(query);
    }

    @GetMapping("/openRedPackage")
    public String openRedPackage(OpenapiRedQuery query) {
        String rid = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.parseLong(rid));
        UserInfoDTO userInfoDTO = rpcService.userInfoDTO(redEnvelope.getUid());
        String url = configService.getOrDefault("telegram_red_share_url"
                , "https://www.assurepro.io/AssureRedpacket/index.html");
        String html = rpcService.html(url);
        html = html.replace("$[username]", userInfoDTO.getNickname());
        html = html.replace("$[shareMoney]", redEnvelope.getAmount().toPlainString() + redEnvelope.getCoin().toUpperCase(Locale.ROOT));
        return html;
    }

}
