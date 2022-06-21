package cn.gudqs7.plugins.common.base.error;

/**
 * 此异常可忽略不要求上报 issue
 *
 * @author wenquan
 * @date 2022/6/21
 */
public class CanIgnoreException extends RuntimeException {

    public CanIgnoreException(String message) {
        super(message);
    }

    public CanIgnoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanIgnoreException(Throwable cause) {
        super(cause);
    }

    protected CanIgnoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
