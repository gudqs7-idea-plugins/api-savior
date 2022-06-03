package cn.gudqs7.plugins.diagnostic;

import cn.gudqs7.plugins.common.diagnostic.AbstractGithubErrorReportSubmitter;
import org.jetbrains.annotations.NotNull;

/**
 * 错误报告处理类
 *
 * @author wenquan
 * @date 2022/5/13
 */
public class SaviorIssueSubmitter extends AbstractGithubErrorReportSubmitter {

    @NotNull
    protected String getGithubRepo() {
        return "docer-savior/docer-savior-idea-plugin";
    }

    @Override
    protected String getGithubToken() {
        return "dG9rZW4gZ2hwX2tUR3JpRDR0WFUxTjJPWk1hVm5DT2lRVllrMGt5ZTBDTTFTTA==";
    }
}
