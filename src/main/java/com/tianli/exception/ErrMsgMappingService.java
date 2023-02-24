package com.tianli.exception;

import com.tianli.common.GoogleTranslator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ErrMsgMappingService {

    public void putThaiMsg(String type, String msg, String transMsg) {
        switch (type) {
            case "en":
                EN_ERR_MSG_PAIRS.put(msg, transMsg);
                break;
            default:
                break;
        }
    }

    public String getTransMsg(String type, String msg) {
        switch (type) {
            case "en":
               return getTransMsg(msg);
//                return EN_ERR_MSG_PAIRS.get(msg);
        }
        return null;
    }

    public String getTransMsg(String msg){
        String enMsg = EN_ERR_MSG_PAIRS.get(msg);
        if (StringUtils.isBlank(enMsg)){
            enMsg = GoogleTranslator.translate(msg);
            if (StringUtils.isNotBlank(enMsg)){
                EN_ERR_MSG_PAIRS.put(msg, enMsg);
            }else{
                enMsg = "System error";
            }
        }
        return enMsg;
    }


    private static ConcurrentHashMap<String, String> EN_ERR_MSG_PAIRS;

    static {
        // 设置定制的语言国家代码
        Locale en = new Locale("en_US");
        // 获得资源文件
        ResourceBundle rben = ResourceBundle.getBundle("message", en);
        Set<String> enKeys = rben.keySet();
        EN_ERR_MSG_PAIRS = new ConcurrentHashMap<>(enKeys.size());
        enKeys.forEach(e -> EN_ERR_MSG_PAIRS.put(e, rben.getString(e)));
    }
}



