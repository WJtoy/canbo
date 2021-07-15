package com.kkl.kklplus.b2b.canbo.http.command;

import com.kkl.kklplus.b2b.canbo.http.request.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class OperationCommand {

    public enum OperationCode {

        GETORDER(1001, "获取已派工单信息", "getOrder.jhtml", GetOrderRequestParam.class),
        GETSENDORDERRESULT(1002, "回传是否接收工单标识", "getSendOrderResult.jhtml",GetSendOrderResultRequestParam.class),
        TOASSIGNENGINEER(1003, "第三方接口调用派工操作", "toAssignEngineer.jhtml",ToAssignEngineerRequestParam.class),
        BOOKORDER(1004, "第三方接口调用预约操作", "bookOrder.jhtml",BookOrderRequestParam.class),
        CANCELORDER(1005, "第三方接口调用取消订单操作", "cancelOrder.jhtml",CancelOrderRequestParam.class),
        ORDERFINISH(1006, "第三方接口调用订单完工操作", "orderFinish.jhtml",OrderFinishRequestParam.class),
        GETORDERINFOCHANGE(1007, "第三方获取工单变更信息", "getOrdersInfochange.jhtml",GetOrderInfoChangeRequestParam.class);

        public int code;
        public String name;
        public String apiUrl;
        public Class reqBodyClass;

        private OperationCode(int code, String name, String apiUrl, Class reqBodyClass) {
            this.code = code;
            this.name = name;
            this.apiUrl = apiUrl;
            this.reqBodyClass = reqBodyClass;
        }
    }

    @Getter
    @Setter
    private OperationCode opCode;

    @Getter
    @Setter
    private RequestParam reqBody;

    public static OperationCommand newInstance(OperationCode opCode, RequestParam reqBody) {
        OperationCommand command = new OperationCommand();
        command.opCode = opCode;
        command.reqBody = reqBody;
        return command;
    }
}
