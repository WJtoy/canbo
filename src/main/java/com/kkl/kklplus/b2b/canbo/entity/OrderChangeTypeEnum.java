package com.kkl.kklplus.b2b.canbo.entity;

public enum OrderChangeTypeEnum {

    MODEIFY(1,"修改"),
    REMARK(2,"备注"),
    REMINDER(3,"催单"),
    CUSTOMER_COMPLIANT(4,"客诉"),
    REJECT_COMPLETE(5,"驳回完工"),
    CONFIRM_COMPLETE(6,"确认完工"),
    CANCELED(7,"取消"),
    DELIEVERED(8,"妥投"),
    ELSE(9,"其他");

    public int value;

    public String name;

    private OrderChangeTypeEnum(int value,String name){
        this.value = value;
        this.name = name;
    }
}
