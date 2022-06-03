package cn.gudqs7.plugins.common.diagnostic;

import cn.gudqs7.plugins.common.util.FreeMarkerUtil;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.Consumer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wenquan
 * @date 2022/5/13
 */
public abstract class AbstractErrorReportSubmitter extends ErrorReportSubmitter {

    protected String getAuthorName() {
        return "Gudqs7";
    }

    @Override
    public @NotNull String getReportActionText() {
        return "Report To " + getAuthorName();
    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        try {
            IdeaLoggingEvent event = events[0];
            String throwableText = event.getThrowableText();
            if (StringUtils.isBlank(throwableText)) {
                return false;
            }
            String message = event.getMessage();
            if (StringUtils.isBlank(message)) {
                message = throwableText.substring(0, throwableText.indexOf("\r\n"));
            }
            if (StringUtils.isBlank(additionalInfo)) {
                additionalInfo = "";
            }

            SubmittedReportInfo reportInfo;
            String issueId = findIssue(throwableText);
            if (issueId == null) {
                issueId = newIssue(throwableText, message, additionalInfo);
                reportInfo = new SubmittedReportInfo(generateUrlByIssueId(issueId), generateTextByIssueId(issueId), SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
            } else {
                reportInfo = new SubmittedReportInfo(generateUrlByIssueId(issueId), generateTextByIssueId(issueId), SubmittedReportInfo.SubmissionStatus.DUPLICATE);
            }
            consumer.consume(reportInfo);
        } catch (Exception e) {
            consumer.consume(new SubmittedReportInfo("", "error: " + e.getMessage(), SubmittedReportInfo.SubmissionStatus.FAILED));
        }
        return true;
    }

    /**
     * 根据 issue id 生成展示文字
     *
     * @param issueId issue id
     * @return 展示文字
     */
    @NotNull
    protected abstract String generateTextByIssueId(String issueId);

    /**
     * 根据 issue id 生成跳转 url
     *
     * @param issueId issue id
     * @return 跳转 url
     */
    @NotNull
    protected abstract String generateUrlByIssueId(String issueId);

    /**
     * 根据错误信息新建 issue
     *
     * @param throwableText  错误栈信息
     * @param message        一般为栈的第一行信息
     * @param additionalInfo 用户输入信息
     * @return issue id
     */
    protected String newIssue(String throwableText, String message, String additionalInfo) {
        String issueMd5 = DigestUtils.md5Hex(throwableText).toUpperCase();
        ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
        PluginDescriptor pluginDescriptor = getPluginDescriptor();
        Properties systemProperties = System.getProperties();
        Map<String, Object> root = new HashMap<>(32);
        root.put("throwableText", throwableText);
        root.put("message", message);
        root.put("additionalInfo", additionalInfo);
        root.put("issueMd5", issueMd5);

        // environment
        root.put("fullApplicationName", appInfo.getFullApplicationName());
        root.put("editionName", ApplicationNamesInfo.getInstance().getEditionName());
        root.put("build", appInfo.getBuild().asString());
        root.put("buildDate", DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(appInfo.getBuildDate().getTime()));
        root.put("systemProperties", systemProperties);
        root.put("javaRuntimeVersion", systemProperties.getProperty("java.runtime.version", systemProperties.getProperty("java.version", "unknown")));
        root.put("osArch", systemProperties.getProperty("os.arch", ""));
        root.put("vmName", systemProperties.getProperty("java.vm.name", "unknown"));
        root.put("vmVendor", systemProperties.getProperty("java.vendor", "unknown"));
        root.put("osInfo", SystemInfo.getOsNameAndVersion());
        root.put("encoding", Charset.defaultCharset().displayName());

        // plugin info
        root.put("pluginName", pluginDescriptor.getName());
        root.put("pluginVersion", pluginDescriptor.getVersion());


        String title = "[Report From Idea] " + message;
        String body = FreeMarkerUtil.renderTemplate("issue.md.ftl", root);
        return newIssue0(title, body);
    }

    /**
     * 根据固定模板生成的问题描述和问题标题生成 issue
     *
     * @param title 标题
     * @param body  描述
     * @return issue id
     */
    protected abstract String newIssue0(String title, String body);

    /**
     * 根据错误栈信息查找 issue id
     *
     * @param throwableText 错误栈信息
     * @return issue id
     */
    protected String findIssue(String throwableText) {
        String throwableMd5 = DigestUtils.md5Hex(throwableText).toUpperCase();
        return findIssue0(throwableMd5);
    }

    /**
     * 根据错误栈信息MD5查找 issue id
     *
     * @param throwableMd5 错误栈信息MD5
     * @return issue id
     */
    protected abstract String findIssue0(String throwableMd5);

}
