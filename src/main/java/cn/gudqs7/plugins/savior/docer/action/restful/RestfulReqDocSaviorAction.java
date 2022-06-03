package cn.gudqs7.plugins.savior.docer.action.restful;

import cn.gudqs7.plugins.savior.docer.action.base.AbstractReqDocerSavior;
import cn.gudqs7.plugins.savior.docer.theme.ThemeHelper;

/**
 * @author wq
 */
public class RestfulReqDocSaviorAction extends AbstractReqDocerSavior {

    public RestfulReqDocSaviorAction() {
        super(ThemeHelper.getRestfulTheme());
    }
}
