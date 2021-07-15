package com.kkl.kklplus.b2b.canbo.http.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class RequestParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 必填，网点登录名称(第三方标识)
     */
    @Getter
    @Setter
    private String appKey = "";

    /**
     * 必填，授权码(服务端提供)
     */
    @Getter
    @Setter
    private String appSecret = "";

}
