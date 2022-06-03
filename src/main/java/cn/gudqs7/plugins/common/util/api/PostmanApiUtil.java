package cn.gudqs7.plugins.common.util.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 * @date 2021/9/21
 */
public class PostmanApiUtil {

    public static void addCollection(String json, String key) {
        String url = "https://api.getpostman.com/collections";
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-API-Key", key);
        headers.put("Content-Type", "application/json");
        String body = HttpUtil.sendHttpWithBody(url, "POST", json, headers);
        System.out.println("addCollection: \n" + body);
    }

    public static void updateCollection(String name, String json, String key) {
        String url = "https://api.getpostman.com/collections";
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-API-Key", key);
        String allCollectionRes = HttpUtil.sendHttpWithBody(url, "GET", null, headers);
        System.out.println("allCollectionRes: \n" + allCollectionRes);

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Map map = gson.fromJson(allCollectionRes, Map.class);
        List<Map<String, Object>> collections = (List<Map<String, Object>>) map.get("collections");
        String collectionUid = null;
        if (CollectionUtils.isNotEmpty(collections)) {
            for (Map<String, Object> collection : collections) {
                Object collName = collection.get("name");
                if (collName != null) {
                    String name0 = collName.toString();
                    if (name0.equals(name)) {
                        collectionUid = (String) collection.get("uid");
                        break;
                    }
                }
            }
        }
        if (collectionUid == null) {
            addCollection(json, key);
        } else {
            updateCollection0(collectionUid, json, key);
        }

    }

    private static void updateCollection0(String collectionUid, String json, String key) {
        String url = "https://api.getpostman.com/collections/"+collectionUid;
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-API-Key", key);
        headers.put("Content-Type", "application/json");
        String body = HttpUtil.sendHttpWithBody(url, "PUT", json, headers);
        System.out.println("updateCollection0: \n" + body);
    }


}
