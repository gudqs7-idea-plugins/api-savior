package cn.gudqs7.plugins.common.util;

import cn.gudqs7.plugins.savior.docer.pojo.ComplexInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * @author wenquan
 * @date 2022/3/31
 */
public class JsonUtil {

    public static <T> T fromJson(String jsonString, Class<T> aClass) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, aClass);
    }

    public static <T> String toJson(T value) {
        return toJson(value, null);
    }

    public static <T> String toJson(T value, TypeAdapter<ComplexInfo> typeAdapter) {
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        if (typeAdapter != null) {
            gsonBuilder.registerTypeAdapter(ComplexInfo.class, typeAdapter);
        }
        Gson gson = gsonBuilder.create();
        return gson.toJson(value);
    }

    public static TypeAdapter<ComplexInfo> getComplexAdapter() {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter jsonWriter, ComplexInfo jsonExampleInfo) throws IOException {
                if (jsonExampleInfo == null) {
                    jsonWriter.nullValue();
                    return;
                }
                Object realVal = jsonExampleInfo.getRealVal();
                if (realVal instanceof Boolean) {
                    Boolean val = (Boolean) realVal;
                    jsonWriter.value(val);
                } else if (realVal instanceof Long) {
                    Long val = (Long) realVal;
                    jsonWriter.value(val);
                } else if (realVal instanceof Double) {
                    Double val = (Double) realVal;
                    jsonWriter.value(val);
                } else if (realVal instanceof Number) {
                    Number val = (Number) realVal;
                    jsonWriter.value(val);
                } else {
                    jsonWriter.value(jsonExampleInfo.toString());
                }

            }

            @Override
            public ComplexInfo read(JsonReader jsonReader) {
                return null;
            }
        };
    }

}
