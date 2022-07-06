package cn.gudqs7.plugins.common.util.jetbrain;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * idea 相关线程工具
 *
 * @author wq
 * @date 2022/6/5
 */
public class IdeaApplicationUtil {

    private static final String INVOKE_LATER_WRAP_ERROR_KEY = "invoke.later.wrap.error";

    public static void invokeAndWait(@NotNull Runnable runnable) {
        String oldVal = System.getProperty(INVOKE_LATER_WRAP_ERROR_KEY);
        try {
            // 设置环境变量以使 com.intellij.openapi.application.impl.LaterInvocator.invokeAndWait() 时
            //  不会 new 异常而是直接抛出, 避免导致异常的代码行在堆栈信息中消失
            System.setProperty(INVOKE_LATER_WRAP_ERROR_KEY, "false");
            ApplicationManager.getApplication().invokeAndWait(() -> {
                try {
                    runnable.run();
                } catch (Throwable ex) {
                    ExceptionUtil.handleException(ex);
                }
            });
        } finally {
            // 最后还原此环境变量
            if (oldVal == null) {
                System.clearProperty(INVOKE_LATER_WRAP_ERROR_KEY);
            } else {
                System.setProperty(INVOKE_LATER_WRAP_ERROR_KEY, oldVal);
            }
        }

    }

    public static void runReadAction(@NotNull ThrowableRunnable<Throwable> action) {
        try {
            ReadAction.run(action);
        } catch (Throwable throwable) {
            ExceptionUtil.handleException(throwable);
        }
    }

}
