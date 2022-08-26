package com.tianli.sso.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminPrivilege {
    Privilege[] or() default {};

    Privilege[] and() default {};

    /**
     * 当路径中存在参数的时候需要指定固定的url
     * eg: /abc/del/uid/{uid}
     *  此时需要在注解中指定api, 因为每次的uid是不一样的, 没办法执行权限校验
     *  @AdminPrivilege(api="/abc/del/uid/uid") 即可;
     */
    String api() default "";
}
