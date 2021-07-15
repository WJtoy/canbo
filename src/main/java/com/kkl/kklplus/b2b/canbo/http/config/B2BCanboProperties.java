package com.kkl.kklplus.b2b.canbo.http.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "canbo")
public class B2BCanboProperties {

    @Getter
    @Setter
    private String requestMainUrl;

    @Getter
    @Setter
    private String appKey;

    @Getter
    @Setter
    private String appSecret;

    @Getter
    @Setter
    private String companyName;

    @Getter
    @Setter
    private Boolean scheduleEnabled;

    @Getter
    private final OkHttpProperties okhttp = new OkHttpProperties();

    public static class OkHttpProperties {
        /**
         * 设置连接超时
         */
        @Getter
        @Setter
        private Integer connectTimeout = 10;

        /**
         * 设置读超时
         */
        @Getter
        @Setter
        private Integer writeTimeout = 10;

        /**
         * 设置写超时
         */
        @Getter
        @Setter
        private Integer readTimeout = 10;

        /**
         * 是否自动重连
         */
        @Getter
        @Setter
        private Boolean retryOnConnectionFailure = true;

        /**
         * 设置ping检测网络连通性的间隔
         */
        @Getter
        @Setter
        private Integer pingInterval = 0;
    }
}
