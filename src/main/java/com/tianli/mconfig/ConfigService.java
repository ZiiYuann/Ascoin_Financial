package com.tianli.mconfig;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.mapper.Config;
import com.tianli.mconfig.mapper.ConfigMapper;
import com.tianli.tool.DataSecurityTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 17:37
 */
@Service
public class ConfigService extends ServiceImpl<ConfigMapper, Config> {

    public static List<String> encryptList = Arrays.asList(
            "main_wallet_password", "s3_AWSAccessKeyId", "s3_AWSSecretAccessKey",
            "tron_private_key"
    );

    public String _get(String name) {
        Config config = configMapper.get(name);
        if (config == null) return null;
        else return this.decrypt(name, config.getValue());
    }

    public String get(String name) {
        String config = configService._get(name);
        if (StringUtils.isEmpty(config)) ErrorCodeEnum.NOT_OPEN.throwException();
        return config;
    }

    public String getAndDecrypt(String name) {
        String config = configService._get(name);
        if (StringUtils.isEmpty(config)) ErrorCodeEnum.NOT_OPEN.throwException();
        return decrypt(name, config);
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
        configMapper.replace(name, value);
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
