package cn.gudqs7.plugins.docer.action.restful;

import cn.gudqs7.plugins.docer.action.base.AbstractReqDocerSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;

/**
 * @author wq
 */
public class RestfulReqDocSaviorAction extends AbstractReqDocerSavior {

    public RestfulReqDocSaviorAction() {
        super(ThemeHelper.getRestfulTheme());
    }
}
