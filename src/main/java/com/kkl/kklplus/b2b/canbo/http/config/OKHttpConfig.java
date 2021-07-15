package com.kkl.kklplus.b2b.canbo.http.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties({B2BCanboProperties.class, B2BTooneProperties.class, B2BFeiyuProperties.class})
@Configuration
public class OKHttpConfig {

    @Bean
    public OkHttpClient okHttpClient(B2BTooneProperties tooneProperties) {
        return new OkHttpClient().newBuilder()
                .connectTimeout(tooneProperties.getOkhttp().getConnectTimeout(), TimeUnit.SECONDS)
                .writeTimeout(tooneProperties.getOkhttp().getWriteTimeout(), TimeUnit.SECONDS)
                .readTimeout(tooneProperties.getOkhttp().getReadTimeout(), TimeUnit.SECONDS)
                .pingInterval(tooneProperties.getOkhttp().getPingInterval(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(tooneProperties.getOkhttp().getRetryOnConnectionFailure())
                .build();
    }

}
