package com.kkl.kklplus.b2b.canbo.http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;

import java.io.IOException;

public class ResponseBodyAdapter extends TypeAdapter<ResponseBody> {

    private static ResponseBodyAdapter adapter = new ResponseBodyAdapter();

    public static ResponseBodyAdapter getInstance() {
        return adapter;
    }

    @Override
    public ResponseBody read(JsonReader in) throws IOException {
        final ResponseBody item = new ResponseBody();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "result_flag":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        item.setErrorCode(in.nextInt());
                    }
                    break;
                case "result_msg":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        item.setErrorMsg(in.nextString());
                    }
                    break;
            }
        }
        in.endObject();
        return item;
    }

    @Override
    public void write(JsonWriter jsonWriter, ResponseBody responseBody) {

    }
}
