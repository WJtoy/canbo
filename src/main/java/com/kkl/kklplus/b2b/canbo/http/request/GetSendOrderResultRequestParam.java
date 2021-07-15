package com.kkl.kklplus.b2b.canbo.http.request;

import com.google.common.collect.Lists;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class GetSendOrderResultRequestParam extends RequestParam {

    /**
     * 必填，0为成功， 1表示接口异常或失败
     */
    private Integer result_flag = 0;

    /**
     * 必填，工单集合
     */
    private List<OrderResult> data = Lists.newArrayList();

    @Data
    public static class OrderResult implements Serializable {

        /**
         * 必填，工单号
         */
        private String orderNo = "";
        /**
         * 必填，是否成功接收了此工单(2成功;1失败)
         */
        private Integer thirdSendFlag;
        /**
         * 必填，接收工单备注信息
         */
        private String thirdSendMessage = "";
        /**
         * 可选，外部单号
         */
        private String outOrderNo;

    }

}
