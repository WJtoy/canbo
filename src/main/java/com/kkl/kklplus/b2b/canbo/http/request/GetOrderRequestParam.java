package com.kkl.kklplus.b2b.canbo.http.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class GetOrderRequestParam extends RequestParam {

    /**
     * 必填，工单类型(1,已派工单)
     */
    @Getter
    @Setter
    private Integer orderType = 1;

    /**
     * 必填，获取工单数量
     */
    @Getter
    @Setter
    private Integer maxQty = 0;

    /**
     * 必填，获取N天内工单数
     */
    @Getter
    @Setter
    private Integer dates = 0;

    /**
     * 必填，厂商名称
     */
    @Getter
    @Setter
    private String companyName = "";

}
