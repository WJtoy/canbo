package com.kkl.kklplus.b2b.canbo.http.request;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ToAssignEngineerRequestParam extends RequestParam {

    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 必填，师傅名称
     */
    private String engineerName = "";

    /**
     * 必填，师傅电话
     */
    private String engineerMobile = "";
}
