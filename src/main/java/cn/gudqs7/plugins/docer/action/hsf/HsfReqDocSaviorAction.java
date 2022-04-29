package cn.gudqs7.plugins.docer.action.hsf;

import cn.gudqs7.plugins.docer.action.base.AbstractReqDocerSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;

/**
 * @author wq
 */
public class HsfReqDocSaviorAction extends AbstractReqDocerSavior {

    public HsfReqDocSaviorAction() {
        super(ThemeHelper.getHsfTheme());
    }
}
