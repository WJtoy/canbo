package com.kkl.kklplus.b2b.canbo.http.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
public class GetOrderInfoChangeRequestParam extends RequestParam {

    /**
     * 必填，获取工单数量
     */
    @Getter
    @Setter
    private String maxQty;

    /**
     * 必填，厂商名称
     */
    @Getter
    @Setter
    private String companyName = "";
    /**
     * 必填，获取N天内工单数
     */
    @Getter
    @Setter
    private String dates;

    @Getter
    @Setter
    private Date beginDate;

    @Getter
    @Setter
    private Date endDate;
}
