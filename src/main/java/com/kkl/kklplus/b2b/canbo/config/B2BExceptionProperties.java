package com.kkl.kklplus.b2b.canbo.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "exception-collect")
public class B2BExceptionProperties {

    private Boolean exceptionEnabled = false;

}
