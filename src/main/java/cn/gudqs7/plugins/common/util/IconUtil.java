package cn.gudqs7.plugins.common.util;

import cn.gudqs7.plugins.common.enums.HttpMethod;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author wq
 * @date 2022/5/28
 */
public class IconUtil {

    public static final Icon ICON_GET = IconLoader.getIcon("/icons/method/get.svg", IconUtil.class);
    public static final Icon ICON_POST = IconLoader.getIcon("/icons/method/post.svg", IconUtil.class);
    public static final Icon ICON_PUT = IconLoader.getIcon("/icons/method/put.svg", IconUtil.class);
    public static final Icon ICON_DELETE = IconLoader.getIcon("/icons/method/delete.svg", IconUtil.class);
    public static final Icon ICON_PATCH = IconLoader.getIcon("/icons/method/patch.svg", IconUtil.class);

    public static Icon getHttpMethodIcon(HttpMethod httpMethod) {
        switch (httpMethod) {
            case POST:
                return IconUtil.ICON_POST;
            case PUT:
                return IconUtil.ICON_PUT;
            case DELETE:
                return IconUtil.ICON_DELETE;
            case PATCH:
                return IconUtil.ICON_PATCH;
            default:
                return IconUtil.ICON_GET;
        }
    }

}
