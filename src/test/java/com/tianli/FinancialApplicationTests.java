package com.tianli;

import com.google.common.base.MoreObjects;
import com.tianli.tool.webhook.DingDingUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class FinancialApplicationTests {

    @Test
    void doTest() {
        DingDingUtil.textType("【申购提醒】\n" +
                        "用户ID: 1736766209569849346\n" +
                        "申购产品：乐创USDT打金二号\n" +
                        "申购金额：0.1 usdt\n" +
                        "申购时间：2022-09-28 09:52:29\n" +
                        "请及时查看哦！",
                "e2462fc274c10ebd0cdc8e3c3e68da89da9cc98fb839ce000c50e96b233bc7e5"
                , "SEC8063b75ee9ef2a2ca0bc8df4a727aeca30a30eeddaef0b8635acbc3585d4b925");
//        SEC8063b75ee9ef2a2ca0bc8df4a727aeca30a30eeddaef0b8635acbc3585d4b925
    }

}
