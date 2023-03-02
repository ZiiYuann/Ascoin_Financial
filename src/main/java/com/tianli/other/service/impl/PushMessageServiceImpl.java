package com.tianli.other.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.other.convert.OtherConvert;
import com.tianli.other.entity.PushMessage;
import com.tianli.other.mapper.PushMessageMapper;
import com.tianli.other.service.PushMessageService;
import com.tianli.other.vo.PushMessageVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-03-02
 **/
@Service
public class PushMessageServiceImpl extends ServiceImpl<PushMessageMapper, PushMessage> implements PushMessageService {

    @Resource
    private OtherConvert otherConvert;

    @Override
    public IPage<PushMessageVO> vos(Page<PushMessage> page, Long uid) {
        return baseMapper.selectPage(page, new LambdaQueryWrapper<PushMessage>()
                        .eq(PushMessage::getUid, uid))
                .convert(otherConvert::toPushMessageVO);
    }
}
