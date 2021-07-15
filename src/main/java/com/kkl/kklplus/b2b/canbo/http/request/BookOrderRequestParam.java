package com.kkl.kklplus.b2b.canbo.http.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
public class BookOrderRequestParam extends RequestParam {

    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 必填，预约人
     */
    private String bookMan = "";

    /**
     * 必填，预约时间(格式：yyyy-MM-dd hh:mm:ss)
     */
    @JsonFormat(
          pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private Date bookDate;

    /**
     * 必填，预约备注信息
     */
    private String bookRemark = "";
}
