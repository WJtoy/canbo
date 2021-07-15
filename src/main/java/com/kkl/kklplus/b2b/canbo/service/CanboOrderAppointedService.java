package com.kkl.kklplus.b2b.canbo.service;

import com.kkl.kklplus.b2b.canbo.mapper.CanboOrderAppointedMapper;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderAppointed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CanboOrderAppointedService {

    @Resource
    private CanboOrderAppointedMapper canboOrderAppointedMapper;

    public void insert(CanboOrderAppointed canboOrderAppointed) {
        canboOrderAppointed.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        canboOrderAppointed.setProcessTime(0);
        canboOrderAppointed.preInsert();
        canboOrderAppointed.setQuarter(QuarterUtils.getQuarter(canboOrderAppointed.getCreateDt()));
        canboOrderAppointedMapper.insert(canboOrderAppointed);
    }

    public void updateProcessFlag(CanboOrderAppointed canboOrderAppointed) {
        canboOrderAppointed.preUpdate();
        canboOrderAppointed.setProcessComment(StringUtils.left(canboOrderAppointed.getProcessComment(), 120));
        canboOrderAppointedMapper.updateProcessFlag(canboOrderAppointed);
    }
}
