package com.kkl.kklplus.b2b.canbo.service;

import com.kkl.kklplus.b2b.canbo.config.B2BExceptionProperties;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BExceptionCollectMQSender;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class B2BExceptionCollectService {

    @Autowired
    private B2BExceptionCollectMQSender b2BExceptionCollectMQSender;

    @Autowired
    private B2BExceptionProperties b2BExceptionProperties;

    /**
     * 异常短信发送
     * @param api 第三方接口名称
     * @param exceptionContent 异常内容
     * @param exceptionType 异常类型
     */
    public void sendExceptionMessage(Integer dataSource,String api,String exceptionContent,int exceptionType){
        if(b2BExceptionProperties != null && b2BExceptionProperties.getExceptionEnabled()) {
            StringBuffer messageContent = new StringBuffer(500);
            messageContent.append(B2BDataSourceEnum.get(dataSource).name);
            messageContent.append("微服务");
            messageContent.append(api);
            messageContent.append("接口出现异常,原因:");
            messageContent.append(StringUtils.left(exceptionContent, 350));
            MQB2BExceptionMessage.B2BExceptionMessage message = MQB2BExceptionMessage.B2BExceptionMessage.newBuilder()
                    .setId(System.currentTimeMillis())
                    .setMobile("15386031338")
                    .setExceptionService(B2BDataSourceEnum.get(dataSource).id)
                    .setExceptionApi(api)
                    .setContent(messageContent.toString())
                    .setSendTime(System.currentTimeMillis())
                    .setExceptionType(exceptionType)
                    .setMessageType(0)
                    .build();
            b2BExceptionCollectMQSender.send(message);
        }
    }
}
