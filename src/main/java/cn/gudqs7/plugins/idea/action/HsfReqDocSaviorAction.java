package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.savior.JavaToApiSavior;
import cn.gudqs7.plugins.idea.savior.JavaToJsonSavior;
import cn.gudqs7.plugins.idea.theme.ThemeHelper;

/**
 * @author wq
 */
public class HsfReqDocSaviorAction extends AbstractReqDocerSavior {

    public HsfReqDocSaviorAction() {
        super(new JavaToJsonSavior(ThemeHelper.getHsfTheme()), new JavaToApiSavior(ThemeHelper.getHsfTheme()));
    }
}
