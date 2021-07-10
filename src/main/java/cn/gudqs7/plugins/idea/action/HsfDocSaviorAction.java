package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.savior.JavaToDocSavior;
import cn.gudqs7.plugins.idea.theme.ThemeHelper;

/**
 * @author wq
 */
public class HsfDocSaviorAction extends AbstractDocerSavior {

    public HsfDocSaviorAction() {
        super(new JavaToDocSavior(ThemeHelper.getHsfTheme()));
    }

}
