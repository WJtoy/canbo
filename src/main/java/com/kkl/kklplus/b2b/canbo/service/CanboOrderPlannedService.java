package com.kkl.kklplus.b2b.canbo.service;

import com.kkl.kklplus.b2b.canbo.mapper.CanboOrderPlannedMapper;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderPlanned;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CanboOrderPlannedService {

    @Resource
    private CanboOrderPlannedMapper canboOrderPlannedMapper;

    public void insert(CanboOrderPlanned canboOrderPlanned) {
        canboOrderPlanned.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        canboOrderPlanned.setProcessTime(0);
        canboOrderPlanned.preInsert();
        canboOrderPlanned.setQuarter(QuarterUtils.getQuarter(canboOrderPlanned.getCreateDt()));
        canboOrderPlannedMapper.insert(canboOrderPlanned);
    }

    public void updateProcessFlag(CanboOrderPlanned canboOrderPlanned) {
        canboOrderPlanned.preUpdate();
        canboOrderPlannedMapper.updateProcessFlag(canboOrderPlanned);
    }
}
