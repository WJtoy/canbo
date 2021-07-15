package com.kkl.kklplus.b2b.canbo.http.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.canbo.http.adapter.ResponseBodyAdapter;
import com.kkl.kklplus.b2b.canbo.http.command.OperationCommand;
import com.kkl.kklplus.b2b.canbo.http.config.B2BCanboProperties;
import com.kkl.kklplus.b2b.canbo.http.config.B2BTooneProperties;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;
import com.kkl.kklplus.b2b.canbo.utils.SpringContextHolder;
import okhttp3.*;

public class OkHttpUtils {

    private static final MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient okHttpClient = SpringContextHolder.getBean(OkHttpClient.class);
    private static B2BTooneProperties tooneProperties = SpringContextHolder.getBean(B2BTooneProperties.class);
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private static Gson responseGson = new GsonBuilder().registerTypeAdapter(ResponseBody.class, ResponseBodyAdapter.getInstance()).create();

    public static <T> ResponseBody<T> postSyncGenericNew(OperationCommand command, Class<T> dataClass, Integer dataSourceId) {
        ResponseBody<T> responseBody = null;
        B2BTooneProperties.DataSourceConfig dataSourceConfig = null;
        if (dataSourceId != null) {
            dataSourceConfig = tooneProperties.getDataSourceConfig().get(dataSourceId);
        }
        if (dataSourceConfig != null && command != null && command.getOpCode() != null &&
                command.getReqBody() != null && command.getReqBody().getClass().getName().equals(command.getOpCode().reqBodyClass.getName())) {
            String url = dataSourceConfig.getRequestMainUrl().concat("/").concat(command.getOpCode().apiUrl);
            command.getReqBody().setAppKey(dataSourceConfig.getAppKey());
            command.getReqBody().setAppSecret(dataSourceConfig.getAppSecret());
            String reqbodyJson = gson.toJson(command.getReqBody());
            RequestBody requestBody = RequestBody.create(CONTENT_TYPE_JSON, reqbodyJson);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("",dataSourceConfig.getAppKey())
                    .post(requestBody)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBodyJson = response.body().string();
                        try {
                            responseBody = gson.fromJson(responseBodyJson, new TypeToken<ResponseBody>() {
                            }.getType());
                            responseBody.setOriginalJson(responseBodyJson);
                            if (responseBody.getErrorCode() == ResponseBody.ErrorCode.SUCCESS.code) {
                                try {
                                    T data = gson.fromJson(responseBodyJson, dataClass);
                                    responseBody.setData(data);
                                } catch (Exception e) {
                                    return new ResponseBody<>(ResponseBody.ErrorCode.DATA_PARSE_FAILURE, e);
                                }
                            }
                        } catch (Exception e) {
                            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.JSON_PARSE_FAILURE, e);
                            responseBody.setOriginalJson(responseBodyJson);
                            return responseBody;
                        }
                    } else {
                        responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_RESPONSE_BODY_ERROR);
                    }
                } else {
                    responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_STATUS_CODE_ERROR);
                }
            } catch (Exception e) {
                return new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE, e);
            }
        } else {
            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_PARAMETER_FORMAT_ERROR);
        }

        return responseBody;
    }

}
