package com.kkl.kklplus.b2b.canbo.http.response;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
public class GetOrderChangeResponseData extends ResponseData {

    @Getter
    @Setter
    private List<CanboOrderChange> data = Lists.newArrayList();

    @Data
    public static class CanboOrderChange {

        private String changeType;
        private String info;
        private String changeBy;
        private String changeDate;
        private String barcode;
        private String urgeTimes;
        private String dispatchStat;
        private String stat;
        private String wishBookDate;
        private String isUrgent;

        /**
         * 必填，工单号
         */
        private String orderNo;

        /**
         * 必填，消费者姓名
         */
        private String userName;

        /**
         * 必填，消费者手机号
         */
        private String userMobile;

        private String userPhone;

        /**
         * 必填，消费者所在省
         */
        private String userProvince;

        /**
         * 必填，消费者所在市
         */
        private String userCity;

        /**
         * 必填，消费者所在区
         */
        private String userCounty;

        /**
         * 必填，消费者详细地址
         */
        private String userAddress;

        /**
         * 必填，维修类型名称
         */
        private String serviceTypeName;

        /**
         * 必填，保修类型(保内/保外)
         */
        private String inOrOut;

        /**
         * 必填，购买时间(格式：yyyy-MM-dd hh:mm:ss)
         */
        private String buyDate;

        /**
         * 必填，购买商场
         */
        private String buyShop;

        /**
         * 可选，品牌
         */
        private String brandName;

        /**
         * 可选，订单来源
         */
        private String orderTypeName;

        /**
         * 可选，订单备注
         */
        private String remark;

        /**
         * 必填，产品明细集合
         */
        private List<Product> items = Lists.newArrayList();

        /**
         * 必填，发布人
         */
        private String isSueBy;

        @Data
        public static class Product {

            /**
             * 必填，产品编码
             */
            private String itemCode;

            /**
             * 必填，产品名称
             */
            private String itemName;

            /**
             * 必填，产品简称
             */
            private String itemShortName;

            /**
             * 必填，产品数量
             */
            private Integer qty;

            /**
             * 必填，产品分类
             */
            private String itemClassName;

        }

    }

}
