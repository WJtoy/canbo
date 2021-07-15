package com.kkl.kklplus.b2b.canbo.http.request;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class OrderFinishRequestParam extends RequestParam {

    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 可选，故障现象编码
     */
    private String pmCode;

    /**
     * 可选，故障原因编码
     */
    private String reasonCode;

    /**
     * 可选，维修措施编码
     */
    private String stepCode;

    /**
     * 可选，完工备注
     */
    private String finishNote;

    /**
     * 必填，工单产品明细集合
     */
    private List<ProductDetail> items = Lists.newArrayList();

    @Data
    public static class ProductDetail implements Serializable {

        /**
         * 必填，产品编码
         */
        private String itemCode = "";

        /**
         * 必填，配件图片
         */
        private String pic1 = "";

        /**
         * 可选，安装卡图片
         */
        private String pic2;

        /**
         * 可选，现场图片
         */
        private String pic3;

        /**
         * 可选，条码图片
         */
        private String pic4;

        /**
         * 可选，完工条码
         */
        private String barcode;

        /**
         * 可选，外机完工条码
         */
        private String outBarcode;

    }
}
