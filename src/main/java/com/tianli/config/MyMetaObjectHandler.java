package com.tianli.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.tianli.sso.permission.admin.AdminContent;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-22
 **/
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // createTime 这个字段与pojo类中字段对应
        LocalDateTime now = LocalDateTime.now();
        this.fillStrategy(metaObject, "createTime", now);
        this.fillStrategy(metaObject, "updateTime", now);
        this.setFieldValByName("createBy", AdminContent.get().getNickname(), metaObject);
        this.setFieldValByName("updateBy", AdminContent.get().getNickname(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateBy", AdminContent.get().getNickname(), metaObject);
    }

}
