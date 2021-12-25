package cn.gudqs7.plugins.docer.action;

import cn.gudqs7.plugins.docer.savior.JavaToApiSavior;
import cn.gudqs7.plugins.docer.savior.JavaToJsonSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;

/**
 * @author wq
 */
public class RestfulReqDocSaviorAction extends AbstractReqDocerSavior {

    public RestfulReqDocSaviorAction() {
        super(new JavaToJsonSavior(ThemeHelper.getRestfulTheme()), new JavaToApiSavior(ThemeHelper.getRestfulTheme()));
    }
}
