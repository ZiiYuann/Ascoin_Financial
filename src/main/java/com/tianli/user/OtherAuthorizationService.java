package com.tianli.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.common.CommonFunction;
import com.tianli.common.FacebookProperties;
import com.tianli.common.HttpUtils;
import com.tianli.common.LineProperties;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapTool;
import com.tianli.tool.RandomStringGeneral;
import com.tianli.tool.judge.JsonObjectTool;
import com.tianli.user.authorize.UserAuthorizeService;
import com.tianli.user.authorize.UserAuthorizeType;
import com.tianli.user.authorize.mapper.UserAuthorize;
import com.tianli.user.dto.FacebookLoginDTO;
import com.tianli.user.dto.LineLoginDTO;
import com.tianli.user.dto.LineTokenDTO;
import com.tianli.user.dto.LineUserInfoDTO;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class OtherAuthorizationService {

    @Resource
    private OtherAuthorizationService otherAuthorizationService;

    @Transactional
    public Map<String, Object> lineByCode(String code) {
        if(otherAuthorizationService.equals(AopContext.currentProxy())){
            System.out.println("666");
        }
        LineUserInfoDTO lineUserInfoDTO = lineLoginByCode(code);
        return verifyLogin(lineUserInfoDTO.getUserId(), new Gson().toJson(lineUserInfoDTO), UserAuthorizeType.line);
    }

    @Transactional
    public Map<String, Object> lineByAccessToken(String accessToken) {
        LineUserInfoDTO lineUserInfoDTO = lineLoginByAccessToken(accessToken);
        return verifyLogin(lineUserInfoDTO.getUserId(), new Gson().toJson(lineUserInfoDTO), UserAuthorizeType.line);
    }

    /**
     * line回调公共类
     */
    private LineUserInfoDTO lineLoginByAccessToken(String accessToken) {
        // id_token 获取 user_info
        Map<String, String> verifyMap = new HashMap<>(4);
        // 封装参数
        verifyMap.put("access_token", accessToken);

        // id_token 获取 user_info
        Map<String, String> verifyMap1 = new HashMap<>(4);
        // 封装参数
        verifyMap1.put("Authorization", "Bearer " +accessToken);
        LineUserInfoDTO lineUserInfoDTO = null;
        try {
            // 正常返回
            // {
            //   "scope":"profile",
            //   "client_id":"1440057261",
            //   "expires_in":2591659
            // }
            // 错误回应
            // {
            //    "error": "invalid_request",
            //    "error_description": "access token expired"
            // }
            HttpResponse getVerifyRes = HttpUtils.doGet(lineProperties.getHost(), lineProperties.getVerify_path(), "GET", Maps.newHashMap(), verifyMap);
            String stringVerifyRes = EntityUtils.toString(getVerifyRes.getEntity());
            Gson gson = new Gson();
            JsonObject jsonObjectVerifyRes = gson.fromJson(stringVerifyRes, JsonObject.class);
            String client_id = jsonObjectVerifyRes.get("client_id").getAsString();
            if(StringUtils.isBlank(client_id) || !StringUtils.equals(client_id, lineProperties.getClient_id())){
                ErrorCodeEnum.LOGIN_AUTHORIZATION_ERROR.throwException();
            }
            // {
            //    "userId": "U9268b867085dfe56b9e080d1ecf123c1",
            //    "displayName": "zxj",
            //    "pictureUrl": "https://profile.line-scdn.net/0hzDL41hurJWtxTTNdkG9aPE0IKwYGYyMjCSk9WF1JKV1ce2VtGShvWFQafg8OKWY7RC85DlZKfFJb"
            // }
            HttpResponse getProfile = HttpUtils.doGet(lineProperties.getHost(), lineProperties.getUser_profile_path(), "GET", verifyMap1, Maps.newHashMap());
            String stringProfile = EntityUtils.toString(getProfile.getEntity());
            // 获取 access_token, id_token
            lineUserInfoDTO = gson.fromJson(stringProfile, LineUserInfoDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Objects.isNull(lineUserInfoDTO) || StringUtils.isEmpty(lineUserInfoDTO.getUserId())) {
            ErrorCodeEnum.ERROR_GETTING_THIRD_PARTY_USER_INFO.throwException();
        }
        return lineUserInfoDTO;
    }
    private LineUserInfoDTO lineLoginByCode(String code) {
        // 1.获取access_token
        Map<String, String> accessMap = new HashMap<>(8);
        // 封装参数
        accessMap.put("client_id", lineProperties.getClient_id());
        accessMap.put("client_secret", lineProperties.getClient_secret());
        accessMap.put("code", code);
        accessMap.put("grant_type", "authorization_code");
        // 请求获取 Getting an access token
        LineTokenDTO lineTokenDTO = null;
        try {
            HttpResponse post = HttpUtils.doPost(lineProperties.getHost(), lineProperties.getToken_path(), "POST", Maps.newHashMap(), Maps.newHashMap(), accessMap);
            String stringResult = EntityUtils.toString(post.getEntity());
            Gson gson = new Gson();
            // 获取 access_token, id_token
            lineTokenDTO = gson.fromJson(stringResult, LineTokenDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Objects.isNull(lineTokenDTO)) {
            ErrorCodeEnum.ERROR_GETTING_THIRD_PARTY_ACCESS_TOKEN.throwException();
        }
        // id_token 获取 user_info
        return lineLoginByAccessToken(lineTokenDTO.getAccess_token());
    }

    /**
     * 已登录状态下绑定line,不修改userInfo的头像昵称
     **/
    public void lineBind(String code, String accessToken) {
        Long uid = requestInitService.uid();
        //校验是否已经绑定过了
        UserAuthorize userAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getUid, uid)
                .eq(UserAuthorize::getType, UserAuthorizeType.line.name()));
        if (Objects.nonNull(userAuthorize)) {
            ErrorCodeEnum.throwException("请勿重复绑定");
        }
        LineUserInfoDTO lineUserInfoDTO;
        if(StringUtils.isNotBlank(code)){
            lineUserInfoDTO = lineLoginByCode(code);
        }else{
            lineUserInfoDTO = lineLoginByAccessToken(accessToken);
        }
        //校验openid是否已经绑定过了
        UserAuthorize openidAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getOpenid, lineUserInfoDTO.getUserId())
                .eq(UserAuthorize::getType, UserAuthorizeType.line.name()));
        if(Objects.isNull(openidAuthorize)){
            ErrorCodeEnum.throwException("请勿重复绑定");
        }
        //user_authorize 绑定表加入数据
        boolean save = userAuthorizeService.save(UserAuthorize.builder()
                .id(CommonFunction.generalId())
                .create_time(LocalDateTime.now())
                .openid(lineUserInfoDTO.getUserId())
                .type(UserAuthorizeType.line)
                .picture(lineUserInfoDTO.getPictureUrl())
                .name(lineUserInfoDTO.getDisplayName())
                .uid(uid).build());
        if (!save) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    private Map<String, Object> verifyLogin(String openid, String cacheUserInfo, UserAuthorizeType type) {
        // 获取到用户的唯一标识
        UserAuthorize authorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getOpenid, openid)
                .eq(UserAuthorize::getType, type));

        // 如果保存了用户信息
        if (authorize != null) {
            // 查询用户
            User user = userService._get(authorize.getUid());
            // 如果用户绑定表存在绑定关系,但是用户表删除此用户,那么删除绑定关系,重新建立绑定关系;
            if (Objects.nonNull(user)) {
                // 账号已经禁用
                if (Objects.equals(UserStatus.disable, user.getStatus())) {
                    ErrorCodeEnum.ACCOUNT_BAND.throwException();
                }
                String token = userTokenService.login(user);

                return MapTool.Map().put("token", token)
                        .put("id", user.getId())
                        .put("newUser", false)
                        .put("initialLogin", false);
            }
        }
        redisTemplate.boundValueOps("temp_user_info_" + openid).set(cacheUserInfo, 5L, TimeUnit.MINUTES);
        return MapTool.Map().put("tempToken", openid)
                .put("initialLogin", true);
    }

    @Transactional
    public Map<String, Object> facebookCallback(String accessToken) {
        JsonObject userInfo = facebookBindCommon(accessToken);
        return verifyLogin(userInfo.get("id").getAsString(), new Gson().toJson(userInfo), UserAuthorizeType.facebook);
    }

    /**
     * 已登录状态下绑定facebook,不修改userInfo的头像昵称
     **/
    public void facebookBind(String accessToken) {
        Long uid = requestInitService.uid();
        //校验是否已经绑定过了
        UserAuthorize userAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getUid, uid)
                .eq(UserAuthorize::getType, UserAuthorizeType.facebook.name()));
        if (Objects.nonNull(userAuthorize)) {
            ErrorCodeEnum.throwException("请勿重复绑定");
        }
        JsonObject userInfo = facebookBindCommon(accessToken);
        //获取facebook返回的用户信息
        String openid = userInfo.get("id").getAsString();
        //获取图片地址
        JsonObject pictureObject = userInfo.get("picture").getAsJsonObject();
        JsonObject data = pictureObject.get("data").getAsJsonObject();
        String picture = data.get("url").getAsString();
        String name = userInfo.get("name").getAsString();
        //校验openid是否已经绑定过了
        UserAuthorize openidAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                .eq(UserAuthorize::getOpenid, openid)
                .eq(UserAuthorize::getType, UserAuthorizeType.facebook.name()));
        if (Objects.nonNull(openidAuthorize)) {
            ErrorCodeEnum.throwException("请勿重复绑定");
        }
        //user_authorize 绑定表加入数据
        boolean save = userAuthorizeService.save(UserAuthorize.builder()
                .id(CommonFunction.generalId())
                .create_time(LocalDateTime.now())
                .openid(openid)
                .type(UserAuthorizeType.facebook)
                .picture(picture)
                .name(name)
                .uid(uid).build());
        if (!save) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    /**
     * facebook回调公共类
     */
    private JsonObject facebookBindCommon(String accessToken) {
        //获取用户信息
        JsonObject userInfo = null;
        try {
            //校验token的有效性
            Map<String, String> map_ = Maps.newHashMap();
            map_.put("access_token", facebookProperties.getClient_id() + "|" + facebookProperties.getClient_secret());
            map_.put("input_token", accessToken);
            HttpResponse get_ = HttpUtils.doGet(facebookProperties.getHost(), facebookProperties.getDebug_token_path(), "Get", Maps.newHashMap(), map_);
            String dataJson_ = EntityUtils.toString(get_.getEntity());
            JsonObject jsonObject = new Gson().fromJson(dataJson_, JsonObject.class);
            Boolean is_valid = JsonObjectTool.getAsBool(jsonObject, "data.is_valid");
            if (Objects.isNull(is_valid) || !is_valid) {
                ErrorCodeEnum.ACCESS_TOKEN_VERIFICATION_FAILED.throwException();
            }
            Map<String, String> map = Maps.newHashMap();
            map.put("access_token", accessToken);
            map.put("fields", "picture{url},name,id");
            map.put("format", "json");
            HttpResponse get = HttpUtils.doGet(facebookProperties.getHost(), facebookProperties.getMe_path(), "Get", Maps.newHashMap(), map);
            String dataJson = EntityUtils.toString(get.getEntity());
            userInfo = new Gson().fromJson(dataJson, JsonObject.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (Objects.isNull(userInfo)) {
            ErrorCodeEnum.ERROR_GETTING_THIRD_PARTY_USER_INFO.throwException();
        }
        return userInfo;
    }

    @Transactional
    public Object lineLogin(LineLoginDTO loginDTO) {
        String temp_token = loginDTO.getTempToken();
        String key = "temp_user_info_" + temp_token;
        Object temp_tokenObj = redisTemplate.boundValueOps(key).get();
        if (Objects.isNull(temp_tokenObj)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        LineUserInfoDTO lineUserInfoDTO = new Gson().fromJson(temp_tokenObj.toString(), LineUserInfoDTO.class);
        Map<String, Object> map = otherLogin(loginDTO.getPhone(), temp_token, lineUserInfoDTO.getDisplayName(), lineUserInfoDTO.getDisplayName(), UserAuthorizeType.line);
        Boolean delete = redisTemplate.delete(key);
        if (Objects.isNull(delete) || !delete) {
            redisTemplate.delete(key);
        }
        return map;
    }

    @Transactional
    public Object facebookLogin(FacebookLoginDTO loginDTO) {
        String temp_token = loginDTO.getTempToken();
        String key = "temp_user_info_" + temp_token;
        Object temp_tokenObj = redisTemplate.boundValueOps(key).get();
        if (Objects.isNull(temp_tokenObj)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        JsonObject jsonObject = new Gson().fromJson(temp_tokenObj.toString(), JsonObject.class);
        Map<String, Object> result = otherLogin(loginDTO.getPhone(), temp_token, JsonObjectTool.getAsString(jsonObject, "picture.data.url"), JsonObjectTool.getAsString(jsonObject, "name"), UserAuthorizeType.facebook);
        Boolean delete = redisTemplate.delete(key);
        if (Objects.isNull(delete) || !delete) {
            redisTemplate.delete(key);
        }
        return result;
    }

    private Map<String, Object> otherLogin(String phone, String temp_token, String avatar, String nick, UserAuthorizeType type) {
        User user = userService._getByUsername(phone);
        boolean newUser = false;
        if (Objects.nonNull(user)) {
            UserAuthorize userAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getUid, user.getId()).eq(UserAuthorize::getType, type));
            if (Objects.nonNull(userAuthorize)) {
                ErrorCodeEnum.throwException("账号已经绑定" + type.name() + "账号");
            }
        }else{
            newUser = true;
            user = userService.reg(phone);
            boolean update = userInfoService.update(new UpdateWrapper<UserInfo>()
                    .set("avatar", avatar)
                    .set("nick", nick)
                    .eq("id", user.getId()));
            if (!update) {
                ErrorCodeEnum.SYSTEM_ERROR.throwException();
            }
        }
        String token = userTokenService.login(user);
        boolean save = userAuthorizeService.save(UserAuthorize.builder()
                .id(CommonFunction.generalId())
                .create_time(LocalDateTime.now())
                .openid(temp_token)
                .type(type)
                .picture(avatar)
                .name(nick)
                .uid(user.getId()).build());
        if (!save) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return MapTool.Map().put("token", token).put("id", user.getId()).put("newUser", newUser);
    }

    @Resource
    private UserService userService;
    @Resource
    private LineProperties lineProperties;
    @Resource
    private FacebookProperties facebookProperties;
    @Resource
    private UserAuthorizeService userAuthorizeService;
    @Resource
    private UserTokenService userTokenService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private ConfigService configService;

    public Object fbCallbackDeletion(String reqParam, String id) {
        if(StringUtils.isNotBlank(reqParam)){

            String[] split = reqParam.split("\\.");
            if(split.length < 2){
                return Maps.newHashMap();
            }
            SignedRequestDecoder signedRequestDecoder = getSignedRequestDecoder();
            Map<String, ?> userInfo;
            try {
                userInfo = signedRequestDecoder.decodeSignedRequest(reqParam);
            }catch (Exception ex){
                return MapTool.Map().put("code", "fail").put("message", "parse signed_request error");
            }
            Object user_id = userInfo.get("user_id");
            if(Objects.isNull(user_id)){
                return MapTool.Map().put("code", "fail").put("message", "parse signed_request get user_id is null");
            }
            //判断是否存在
            UserAuthorize facebook = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                    .eq(UserAuthorize::getOpenid, user_id)
                    .eq(UserAuthorize::getType, UserAuthorizeType.facebook.name()));
            if (Objects.isNull(facebook)) return Maps.newHashMap();
            String randomCode = RandomStringGeneral.general(8);
            BoundValueOperations<String, Object> boundValueOps = redisTemplate.boundValueOps("fb:deletion:id:" + randomCode);
            boundValueOps.set(user_id, 30, TimeUnit.DAYS);
            userAuthorizeService.remove(new LambdaQueryWrapper<UserAuthorize>()
                    .eq(UserAuthorize::getOpenid, user_id).eq(UserAuthorize::getType, UserAuthorizeType.facebook.name()));

            return MapTool.Map()
                    .put("url", configService.getOrDefaultNoCache("url", "https://www.BFpacepro.com/api") + "/user/facebook/relieve?id="+randomCode)
                    .put("confirmation_code", randomCode);
        }

        if(StringUtils.isNotBlank(id)){
            BoundValueOperations<String, Object> boundValueOps = redisTemplate.boundValueOps("fb:deletion:id:" + id);
            Object idCache = boundValueOps.get();
            if(Objects.isNull(idCache)){
                return MapTool.Map().put("code", "success")
                        .put("msg", "Delete success");
            }
            //判断是否存在
            UserAuthorize facebook = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                    .eq(UserAuthorize::getOpenid, idCache.toString())
                    .eq(UserAuthorize::getType, UserAuthorizeType.facebook.name()));
            if (Objects.isNull(facebook)) return MapTool.Map().put("code", "success").put("msg", "Delete success");
            boolean remove = userAuthorizeService.remove(new LambdaQueryWrapper<UserAuthorize>()
                    .eq(UserAuthorize::getOpenid, idCache.toString()).eq(UserAuthorize::getType, UserAuthorizeType.facebook.name()));
            redisTemplate.delete("fb:deletion:id:" + id);
            if (remove) return MapTool.Map().put("code", "success").put("msg", "Delete success");

        }
        return Maps.newHashMap();
    }

    private volatile SignedRequestDecoder signedRequestDecoder;

    private SignedRequestDecoder getSignedRequestDecoder(){
        if(Objects.nonNull(signedRequestDecoder)){
            return signedRequestDecoder;
        }
        synchronized (this){
            if(Objects.nonNull(signedRequestDecoder)){
                return signedRequestDecoder;
            }
            String client_secret = facebookProperties.getClient_secret();
            signedRequestDecoder = new SignedRequestDecoder(client_secret);
            return signedRequestDecoder;
        }
    }
}
