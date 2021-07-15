package com.kkl.kklplus.b2b.canbo.handler;

import com.kkl.kklplus.b2b.canbo.http.config.B2BFeiyuProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 飞羽安全验证
 * @author: Jeff.Zhao
 * @date: 2018/8/20 9:59
 */
@Slf4j
@Configuration
public class FeiyuSecurityHandler extends HandlerInterceptorAdapter {
    @Autowired
    private B2BFeiyuProperties feiyuProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String appKey = "";
        String appSecret = "";
        try {
            appKey = request.getHeader("appKey") == null ? "" : request.getHeader("appKey");
            appSecret = request.getHeader("appSecret") == null ? "" : request.getHeader("appSecret");
        }catch (Exception e){
            log.error("权限验证", e);
        }
        if (appKey.equals(feiyuProperties.getAppKey()) &&
            appSecret.equals(feiyuProperties.getAppSecret())){
            return true;
        }
        throw new Exception("非法请求,身份验证失败.");
    }
}
