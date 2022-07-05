package com.tianli.mconfig;

import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.mapper.Config;
import com.tianli.mconfig.mapper.ConfigMapper;
import com.tianli.tool.DataSecurityTool;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 17:37
 */
@Service
public class ConfigService {

    public static List<String> encryptList = Arrays.asList(
            "main_wallet_password", "s3_AWSAccessKeyId", "s3_AWSSecretAccessKey",
            "tron_private_key", "cm_token"
    );

    //    @Cacheable(value = "ConfigService.get", key = "#name")
    public String _get(String name) {
        Config config = configMapper.get(name);
        if (config == null) return null;
        else return this.decrypt(name, config.getValue());
    }

    public String _get_pure(String name) {
        Config config = configMapper.get(name);
        if (config == null) return null;
        else return config.getValue();
    }


    public String get(String name) {
        String config = configService._get(name);
        if (StringUtils.isEmpty(config)) ErrorCodeEnum.NOT_OPEN.throwException();
        return config;
    }

    public String getNoCache(String name) {
        String config = configService.get(name);
        if (config == null) ErrorCodeEnum.NOT_OPEN.throwException();
        return config;
    }

    public String getOrDefaultNoCache(String name, String value) {
        String config = configService._get(name);
        if (config == null) {
            return value;
        }
        return config;
    }

    public String getOrDefault(String name, String value) {
        String config = configService._get(name);
        if (config == null) {
            return value;
        }
        return config;
    }


    @CacheEvict(value = "ConfigService.get", key = "#config.name")
    public long insert(Config config) {
        return configMapper.insert(config);
    }

    public boolean cas(String name, String oldValue, String newValue) {
        return configMapper.update(name, oldValue, newValue) > 0L;
    }

    public void replace(String name, String value) {
        configMapper.replaceParam(name, value);
    }

    public String decrypt(String name, String value) {
        if(encryptList.contains(name)) return dataSecurityTool.decryptWithPrivateKey(value);
        return value;
    }


    @Resource
    private ConfigService configService;
    @Resource
    private ConfigMapper configMapper;
    @Resource
    private DataSecurityTool dataSecurityTool;

}
