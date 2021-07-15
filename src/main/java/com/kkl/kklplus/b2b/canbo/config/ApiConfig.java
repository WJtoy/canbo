package com.kkl.kklplus.b2b.canbo.config;

import com.kkl.kklplus.b2b.canbo.handler.FeiyuSecurityHandler;
import com.kkl.kklplus.b2b.canbo.http.config.B2BFeiyuProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.stream.Collectors;

/**
 *
 * @author: Jeff.Zhao
 * @date: 2018/8/20 10:08
 */
@Configuration
public class ApiConfig extends WebMvcConfigurerAdapter {
    @Autowired
    FeiyuSecurityHandler feiyuSecurityHandler;
    @Autowired
    B2BFeiyuProperties feiyuProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(feiyuSecurityHandler).addPathPatterns(feiyuProperties.getMethods());
        super.addInterceptors(registry);
    }
}
