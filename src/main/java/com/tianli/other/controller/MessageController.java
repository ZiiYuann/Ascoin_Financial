package com.tianli.other.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.other.entity.PushMessage;
import com.tianli.other.service.PushMessageService;
import com.tianli.other.vo.PushMessageVO;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-03-02
 **/
@RestController
@RequestMapping("/other")
public class MessageController {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private PushMessageService pushMessageService;

    @GetMapping("/message")
    public Result<IPage<PushMessageVO>> messageList(PageQuery<PushMessage> page) {
        IPage<PushMessageVO> result = pushMessageService.vos(page.page(), requestInitService.uid());
        return new Result<>(result);
    }

}
