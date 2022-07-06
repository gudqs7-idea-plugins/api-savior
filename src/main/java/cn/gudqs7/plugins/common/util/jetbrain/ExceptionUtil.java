package cn.gudqs7.plugins.common.util.jetbrain;

import cn.gudqs7.plugins.common.base.error.CanIgnoreException;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.Lombok;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author wq
 */
public class ExceptionUtil {

    public static void logException(Throwable throwable) {
        logException(throwable, "");
    }

    public static void logException(Throwable throwable, String addition) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        String stackTrace = writer.getBuffer().toString();
        NotificationUtil.showError("插件运行失败, " + addition + "错误信息如下: " + stackTrace);
    }

    public static void handleException(Throwable throwable) {
        if (throwable instanceof ProcessCanceledException) {
            throw Lombok.sneakyThrow(throwable);
        }
        if (throwable instanceof CanIgnoreException) {
            logException(throwable, "");
        } else {
            String addition = "可通过 IDEA 右下角感叹号, 点击 Report To Gudqs7(或 Report And Clear All) 一键上报到 GitHub Issue; " +
                    "\n另外, 请在上报异常时, 填入您的联系信息, 或 issue 生成后点击进入页面留言以获得 issue 进展通知!" +
                    "\n";
            logException(throwable, addition);
            throw Lombok.sneakyThrow(throwable);
        }
    }

    public static void handleSyntaxError(String code) throws RuntimeException {
        throw new CanIgnoreException("您的代码可能存在语法错误, 无法为您生成代码, 参考信息: " + code);
    }
}
