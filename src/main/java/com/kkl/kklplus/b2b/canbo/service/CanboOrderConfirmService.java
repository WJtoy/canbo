package com.kkl.kklplus.b2b.canbo.service;


import com.kkl.kklplus.b2b.canbo.entity.CanboOrderConfirm;
import com.kkl.kklplus.b2b.canbo.mapper.CanboOrderConfirmedMapper;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CanboOrderConfirmService {

    @Resource
    private CanboOrderConfirmedMapper canboOrderConfirmedMapper;

    @Transactional()
    public void insert(List<CanboOrderConfirm> canboOrderConfirms) {
        for(CanboOrderConfirm canboOrderConfirm : canboOrderConfirms) {
            if(canboOrderConfirm.getB2bOrderId() == null){
                canboOrderConfirm.setB2bOrderId(0L);
            }
            canboOrderConfirm.setResultFlag(0);
            canboOrderConfirm.setCreateById(1L);
            canboOrderConfirm.setUpdateById(1L);
            canboOrderConfirm.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
            canboOrderConfirm.setProcessTime(0);
            canboOrderConfirm.preInsert();
            canboOrderConfirm.setQuarter(QuarterUtils.getQuarter(canboOrderConfirm.getCreateDt()));
            canboOrderConfirmedMapper.insert(canboOrderConfirm);
        }
    }

    public void updateProcessFlag(List<CanboOrderConfirm> canboOrderConfirms,
                                  Integer processFlag,
                                  String processComment,
                                  Long updateDate) {
        canboOrderConfirmedMapper.updateProcessFlag(canboOrderConfirms,
                processFlag,processComment,updateDate);
    }
}


