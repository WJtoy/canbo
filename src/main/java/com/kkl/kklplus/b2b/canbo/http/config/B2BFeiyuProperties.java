package com.kkl.kklplus.b2b.canbo.http.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 飞羽配置文件
 * @author: Jeff.Zhao
 * @date: 2018/8/20 9:50
 */
@ConfigurationProperties(prefix = "feiyu")
public class B2BFeiyuProperties {
    @Getter
    @Setter
    private String appKey;

    @Getter
    @Setter
    private String appSecret;

    @Getter
    @Setter
    private String[] methods;

    @Getter
    @Setter
    private Integer dataSource;
}
