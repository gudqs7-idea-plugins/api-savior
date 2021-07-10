package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.savior.JavaToDocSavior;
import cn.gudqs7.plugins.idea.theme.ThemeHelper;

/**
 * @author wq
 */
public class RestfulDocSaviorAction extends AbstractDocerSavior {

    public RestfulDocSaviorAction() {
        super(new JavaToDocSavior(ThemeHelper.getRestfulTheme()));
    }

}
