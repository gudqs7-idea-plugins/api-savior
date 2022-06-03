package cn.gudqs7.plugins.common.util.jetbrain;

import lombok.Lombok;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author wq
 */
public class ExceptionUtil {

    public static void handleException(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        String stackTrace = writer.getBuffer().toString();
        NotificationUtil.showError("插件运行失败, 可通过 IDEA 右下角感叹号, 点击 Report To Gudqs7(或 Report And Clear All) 一键上报到 GitHub; 错误信息如下: " + stackTrace);
        throw Lombok.sneakyThrow(throwable);
    }

    public static void handleSyntaxError(String code) throws RuntimeException {
        throw new RuntimeException("您的代码可能存在语法错误, 无法为您生成代码, 参考信息: " + code);
    }
}
