package com.kkl.kklplus.b2b.canbo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CanboUtils {

    public static final String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";

    public final static String REQUESTMETHOD = "POST";
    /**
     * 分页查询的地址
     */
    public final static String ORDERLIST = "/orderInfo/getList";
    /**
     * 检查工单的地址
     */
    public final static String CHECKPROCESSFLAG = "/orderInfo/checkWorkcardProcessFlag";
    /**
     * 更新工单状态的地址
     */
    public final static String UPDATETRANSFERRESULT = "/orderInfo/updateTransferResult";

    public final static int commentLength = 250;

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static String toGson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromGson(String json, Class<T> classOfT) {
        return gson.fromJson(json,classOfT);
    }

    public static String cutOutErrorMessage(String errorMessage){
       if(errorMessage == null){
           return null;
       }
       return errorMessage.length() > CanboUtils.commentLength
                ? errorMessage.substring(0,CanboUtils.commentLength):errorMessage;
    }

    public static String cleanHtmlTag(String content) {
        return content.replaceAll(RE_HTML_MARK, "");
    }
}
