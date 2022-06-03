package cn.gudqs7.plugins.savior.action.restful;

import cn.gudqs7.plugins.savior.action.base.AbstractReqDocerSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;

/**
 * @author wq
 */
public class RestfulReqDocSaviorAction extends AbstractReqDocerSavior {

    public RestfulReqDocSaviorAction() {
        super(ThemeHelper.getRestfulTheme());
    }
}
