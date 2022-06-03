package cn.gudqs7.plugins.savior.docer.util;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.collections.CollectionUtils;

import java.text.DecimalFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Github Api 工具
 *
 * @author wenquan
 * @date 2022/5/16
 */
public class GithubApiUtil {

    private static final String GITHUB_REPOSITORY = "docer-savior/docer-savior-idea-plugin";
    private static final String GITHUB_TOKEN = "dG9rZW4gZ2hwX2tUR3JpRDR0WFUxTjJPWk1hVm5DT2lRVllrMGt5ZTBDTTFTTA==";
    private static final String API_BASE_URL = "https://api.github.com";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

    public static String searchIssue(String body) {
        try {
            Map<String, String> headers = null;
            String q = "repo:" + GITHUB_REPOSITORY + " is:issue in:body " + body;
            URLCodec urlCodec = new URLCodec("UTF-8");
            String query = "q=" + urlCodec.encode(q) + "&page=1&per_page=1";
            String result = HttpUtil.sendHttpWithBody(API_BASE_URL + "/search/issues?" + query, "GET", null, headers);
            System.out.println("searchIssue res :: " + result);
            Map map = JsonUtil.fromJson(result, Map.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("items");
            if (CollectionUtils.isNotEmpty(items)) {
                Map<String, Object> item0 = items.get(0);
                Object number = item0.get("number");
                if (number != null) {
                    return DECIMAL_FORMAT.format(number);
                }
            }
        } catch (Exception e) {
            ActionUtil.handleException(e);
        }
        return null;
    }

    public static String newIssue(String title, String body) {
        try {
            Map<String, String> headers = getAuthHeader();
            Map<String, Object> json = new HashMap<>(4);
            json.put("title", title);
            json.put("body", body);
            String result = HttpUtil.sendHttpWithBody(API_BASE_URL + "/repos/" + GITHUB_REPOSITORY + "/issues", "POST", JsonUtil.toJson(json), headers);
            System.out.println("newIssue res :: " + result);
            Map map = JsonUtil.fromJson(result, Map.class);
            Object number = map.get("number");
            if (number != null) {
                return DECIMAL_FORMAT.format(number);
            }
        } catch (Exception e) {
            ActionUtil.handleException(e);
        }
        return null;
    }

    private static Map<String, String> getAuthHeader() {
        Map<String, String> head = new HashMap<>(2);
        head.put("Authorization", new String(Base64.getDecoder().decode(GITHUB_TOKEN)));
        return head;
    }

}
