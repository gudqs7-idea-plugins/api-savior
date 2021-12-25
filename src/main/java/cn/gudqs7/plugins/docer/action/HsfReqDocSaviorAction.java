package cn.gudqs7.plugins.docer.action;

import cn.gudqs7.plugins.docer.savior.JavaToApiSavior;
import cn.gudqs7.plugins.docer.savior.JavaToJsonSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;

/**
 * @author wq
 */
public class HsfReqDocSaviorAction extends AbstractReqDocerSavior {

    public HsfReqDocSaviorAction() {
        super(new JavaToJsonSavior(ThemeHelper.getHsfTheme()), new JavaToApiSavior(ThemeHelper.getHsfTheme()));
    }
}
