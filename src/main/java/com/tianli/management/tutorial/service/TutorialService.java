package com.tianli.management.tutorial.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.management.tutorial.mapper.Tutorial;
import com.tianli.management.tutorial.mapper.TutorialMapper;
import org.springframework.stereotype.Service;

/**
 * @author chensong
 * @date 2021-02-24 14:35
 * @since 1.0.0
 */
@Service
public class TutorialService extends ServiceImpl<TutorialMapper, Tutorial> {
}
