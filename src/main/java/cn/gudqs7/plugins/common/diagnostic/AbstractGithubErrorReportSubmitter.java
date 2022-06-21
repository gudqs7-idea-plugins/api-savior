package cn.gudqs7.plugins.common.diagnostic;

import cn.gudqs7.plugins.common.consts.CommonConst;
import cn.gudqs7.plugins.common.util.JsonUtil;
import cn.gudqs7.plugins.common.util.api.HttpUtil;
import lombok.SneakyThrows;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHub issue 报告基类
 *
 * @author wq
 * @date 2022/6/3
 */
public abstract class AbstractGithubErrorReportSubmitter extends AbstractErrorReportSubmitter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");
    private static final String API_BASE_URL = "https://api.github.com";

    /**
     * 获取 GitHub 仓库地址, 即用户名+仓库名
     *
     * @return GitHub 仓库地址
     */
    @NotNull
    protected abstract String getGithubRepo();

    /**
     * 获取汇报者 GitHub token
     *
     * @return 汇报者 GitHub token
     */
    protected abstract String getGithubToken();

    @Override
    protected String getIssueListPageUrl() {
        return "https://github.com/" + getGithubRepo() + "/issues";
    }

    @NotNull
    @Override
    protected String generateTextByIssueId(String issueId) {
        return "Github Issue#" + issueId;
    }

    @NotNull
    @Override
    protected String generateUrlByIssueId(String issueId) {
        return "https://github.com/" + getGithubRepo() + "/issues/" + issueId;
    }

    @NotNull
    @Override
    protected String newIssueByTitleBody(String title, String body) {
        Map<String, String> headers = getAuthHeader();
        Map<String, Object> json = new HashMap<>(4);
        json.put("title", title);
        json.put("body", body);
        String result = HttpUtil.sendHttpWithBody(API_BASE_URL + "/repos/" + getGithubRepo() + "/issues", "POST", JsonUtil.toJson(json), headers);
        Map map = JsonUtil.fromJson(result, Map.class);
        Object number = map.get("number");
        return DECIMAL_FORMAT.format(number);
    }

    @SneakyThrows
    @Override
    protected String findIssueByMd5(String throwableMd5) {
        String q = "repo:" + getGithubRepo() + " is:issue in:body " + throwableMd5;
        URLCodec urlCodec = new URLCodec(CommonConst.UTF8);
        String query = "q=" + urlCodec.encode(q) + "&page=1&per_page=1";
        String result = HttpUtil.sendHttpWithBody(API_BASE_URL + "/search/issues?" + query, "GET", null, null);
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
        return null;
    }

    private Map<String, String> getAuthHeader() {
        Map<String, String> head = new HashMap<>(2);
        head.put("Authorization", new String(Base64.getDecoder().decode(getGithubToken())));
        return head;
    }

}
