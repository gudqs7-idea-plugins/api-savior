package cn.gudqs7.plugins.error;

import cn.gudqs7.plugins.docer.util.FreeMarkerUtil;
import cn.gudqs7.plugins.docer.util.GithubApiUtil;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wenquan
 * @date 2022/5/13
 */
public class ReportSubmitter extends ErrorReportSubmitter {

    private static final String GITHUB_REPOSITORY = "docer-savior/docer-savior-idea-plugin";
    private static final String ISSUE_URL = "https://github.com/docer-savior/docer-savior-idea-plugin/issues";

    @Override
    public @NotNull String getReportActionText() {
        return "Report To Gudqs7";
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
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
                reportInfo = new SubmittedReportInfo(ISSUE_URL + "/" + issueId,
                        "Issue#" + issueId, SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
            } else {
                reportInfo = new SubmittedReportInfo(ISSUE_URL + "/" + issueId,
                        "Issue#" + issueId, SubmittedReportInfo.SubmissionStatus.DUPLICATE);
            }
            consumer.consume(reportInfo);
        } catch (Exception e) {
            consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED));
        }
        return true;
    }

    private String newIssue(String throwableText, String message, String additionalInfo) {
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

        // plugin info
        root.put("pluginName", pluginDescriptor.getName());
        root.put("pluginVersion", pluginDescriptor.getVersion());


        String title = "[Report From Idea] " + message;
        String body = FreeMarkerUtil.renderTemplate("issue.md.ftl", root);
        return GithubApiUtil.newIssue(title, body);
    }

    private String findIssue(String throwableText) {
        String md5Hex = DigestUtils.md5Hex(throwableText).toUpperCase();
        return GithubApiUtil.searchIssue(md5Hex);
    }
}
