package cn.gudqs7.plugins.search.icon;

import cn.gudqs7.plugins.common.enums.HttpMethod;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author wq
 * @date 2022/5/28
 */
public class IconHolder {

    public static final Icon ICON_GET = IconLoader.getIcon("/icons/method/get.svg", IconHolder.class);
    public static final Icon ICON_POST = IconLoader.getIcon("/icons/method/post.svg", IconHolder.class);
    public static final Icon ICON_PUT = IconLoader.getIcon("/icons/method/put.svg", IconHolder.class);
    public static final Icon ICON_DELETE = IconLoader.getIcon("/icons/method/delete.svg", IconHolder.class);
    public static final Icon ICON_PATCH = IconLoader.getIcon("/icons/method/patch.svg", IconHolder.class);

    public static Icon getHttpMethodIcon(HttpMethod httpMethod) {
        switch (httpMethod) {
            case POST:
                return IconHolder.ICON_POST;
            case PUT:
                return IconHolder.ICON_PUT;
            case DELETE:
                return IconHolder.ICON_DELETE;
            case PATCH:
                return IconHolder.ICON_PATCH;
            default:
                return IconHolder.ICON_GET;
        }
    }

}
