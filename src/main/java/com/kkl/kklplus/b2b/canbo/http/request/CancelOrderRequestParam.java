package com.kkl.kklplus.b2b.canbo.http.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
public class CancelOrderRequestParam extends RequestParam {

    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 必填，取消人
     */
    private String cancelMan = "";

    /**
     * 必填，取消时间(格式：yyyy-MM-dd hh:mm:ss)
     */
    @JsonFormat(
          pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private Date cancelDate;

    /**
     * 必填，取消原因
     */
    private String cancelRemark = "";
}
