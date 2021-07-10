package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.savior.JavaToApiSavior;
import cn.gudqs7.plugins.idea.savior.JavaToJsonSavior;
import cn.gudqs7.plugins.idea.theme.ThemeHelper;

/**
 * @author wq
 */
public class RestfulReqDocSaviorAction extends AbstractReqDocerSavior {

    public RestfulReqDocSaviorAction() {
        super(new JavaToJsonSavior(ThemeHelper.getRestfulTheme()), new JavaToApiSavior(ThemeHelper.getRestfulTheme()));
    }
}
