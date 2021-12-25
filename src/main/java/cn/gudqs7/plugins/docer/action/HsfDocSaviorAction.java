package cn.gudqs7.plugins.docer.action;

import cn.gudqs7.plugins.docer.savior.JavaToDocSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;

/**
 * @author wq
 */
public class HsfDocSaviorAction extends AbstractDocerSavior {

    public HsfDocSaviorAction() {
        super(new JavaToDocSavior(ThemeHelper.getHsfTheme()));
    }

}
