package com.tianli.agent.management.service;

import com.tianli.agent.management.bo.AuthUserBO;
import com.tianli.agent.management.bo.RePwdBO;
import com.tianli.agent.management.vo.LoginTokenVO;

public interface AuthService {

    LoginTokenVO login(AuthUserBO loginUserBO);

    void changePassword(RePwdBO rePwdBO);


}
