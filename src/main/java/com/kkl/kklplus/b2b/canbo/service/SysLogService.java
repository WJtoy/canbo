package com.kkl.kklplus.b2b.canbo.service;


import com.kkl.kklplus.b2b.canbo.entity.SysLog;
import com.kkl.kklplus.b2b.canbo.mapper.SysLogMapper;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SysLogService {

    private static final int DICT_LOCK_EXPIRED  = 60;

    @Resource
    private SysLogMapper sysLogMapper;


    /**
     * 添加异常日志
     */
    public void insert(Integer dataSource,Long createId, String params,String exception, String title,String uri,String method){
        SysLog sysLog = new SysLog();
        sysLog.setDataSource(dataSource);
        sysLog.setCreateDt(System.currentTimeMillis());
        sysLog.setType(1);
        sysLog.setCreateById(createId);
        sysLog.setRequestUri(uri);
        sysLog.setMethod(method);
        sysLog.setParams(params);
        sysLog.setException(exception);
        sysLog.setTitle(title);
        sysLog.setQuarter(QuarterUtils.getQuarter(new Date(sysLog.getCreateDt())));
        try {
            sysLogMapper.insert(sysLog);
        }catch (Exception e){
            log.error("报错信息记录失败",e.getMessage());
        }
    }

    public void insert(Long createId, String params,String exception, String title,String uri,String method){
        SysLog sysLog = new SysLog();
        sysLog.setCreateDt(System.currentTimeMillis());
        sysLog.setType(1);
        sysLog.setCreateById(createId);
        sysLog.setRequestUri(uri);
        sysLog.setMethod(method);
        sysLog.setParams(params);
        sysLog.setException(exception);
        sysLog.setTitle(title);
        sysLog.setQuarter(QuarterUtils.getQuarter(new Date(sysLog.getCreateDt())));
        try {
            sysLogMapper.insert(sysLog);
        }catch (Exception e){
            log.error("报错信息记录失败",e.getMessage());
        }
    }
}
