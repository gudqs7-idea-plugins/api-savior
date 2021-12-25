package cn.gudqs7.plugins.docer.action;

import cn.gudqs7.plugins.docer.savior.JavaToDocSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;

/**
 * @author wq
 */
public class RestfulDocSaviorAction extends AbstractDocerSavior {

    public RestfulDocSaviorAction() {
        super(new JavaToDocSavior(ThemeHelper.getRestfulTheme()));
    }

}
