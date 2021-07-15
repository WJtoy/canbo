package com.kkl.kklplus.b2b.canbo.service;


import com.kkl.kklplus.b2b.canbo.mapper.CanboOrderCompletedMapper;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CanboOrderCompletedService {

    @Resource
    private CanboOrderCompletedMapper canboOrderCompletedMapper;

    public void insert(CanboOrderCompleted canboOrderCompleted) {
        canboOrderCompleted.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        canboOrderCompleted.setProcessTime(0);
        canboOrderCompleted.preInsert();
        canboOrderCompleted.setQuarter(QuarterUtils.getQuarter(canboOrderCompleted.getCreateDt()));
        canboOrderCompletedMapper.insert(canboOrderCompleted);
    }

    public void updateProcessFlag(CanboOrderCompleted canboOrderCompleted) {
        canboOrderCompleted.preUpdate();
        canboOrderCompletedMapper.updateProcessFlag(canboOrderCompleted);
    }
}
