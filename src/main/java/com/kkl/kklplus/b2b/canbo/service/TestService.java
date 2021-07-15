package com.kkl.kklplus.b2b.canbo.service;

import com.kkl.kklplus.b2b.canbo.entity.SysLog;
import com.kkl.kklplus.b2b.canbo.http.command.OperationCommand;
import com.kkl.kklplus.b2b.canbo.http.config.B2BCanboProperties;
import com.kkl.kklplus.b2b.canbo.http.request.GetOrderRequestParam;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;
import com.kkl.kklplus.b2b.canbo.http.response.GetOrderResponseData;
import com.kkl.kklplus.b2b.canbo.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.canbo.mapper.SysLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TestService {

    @Resource
    private SysLogMapper sysLogMapper;

    @Autowired
    private B2BCanboProperties canboProperties;

    public ResponseBody<GetOrderResponseData> test() {
        GetOrderRequestParam reqBody = new GetOrderRequestParam();
        reqBody.setOrderType(1);
        reqBody.setMaxQty(20);
        reqBody.setDates(20);
        reqBody.setCompanyName(canboProperties.getCompanyName());

        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.GETORDER, reqBody);

        //ResponseBody<GetOrderResponseData> resBody = OkHttpUtils.postSyncGeneric(command, GetOrderResponseData.class);
        //String detailMessage = resBody.getErrorDetailMsg();
        return null;
    }

    public SysLog test2() {
        return sysLogMapper.get(100L);
    }
}


