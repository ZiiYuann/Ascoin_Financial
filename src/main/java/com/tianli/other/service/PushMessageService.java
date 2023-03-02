package com.tianli.other.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.other.entity.PushMessage;
import com.tianli.other.vo.PushMessageVO;

/**
 * @author chenb
 * @apiNote
 * @since 2023-03-02
 **/
public interface PushMessageService extends IService<PushMessage> {

    IPage<PushMessageVO> vos(Page<PushMessage> page, Long uid);

}
