package com.kkl.kklplus.b2b.canbo.service;

import com.kkl.kklplus.b2b.canbo.mapper.CanboOrderCancelledMapper;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCancelled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CanboOrderCancelledService {

    @Resource
    private CanboOrderCancelledMapper canboOrderCancelledMapper;

    public void insert(CanboOrderCancelled canboOrderCancelled) {
        canboOrderCancelled.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        canboOrderCancelled.setProcessTime(0);
        canboOrderCancelled.preInsert();
        canboOrderCancelled.setQuarter(QuarterUtils.getQuarter(canboOrderCancelled.getCreateDt()));
        canboOrderCancelledMapper.insert(canboOrderCancelled);
    }

    public void updateProcessFlag(CanboOrderCancelled canboOrderCancelled) {
        canboOrderCancelled.preUpdate();
        canboOrderCancelledMapper.updateProcessFlag(canboOrderCancelled);
    }
}
