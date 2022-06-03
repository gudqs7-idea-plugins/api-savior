package cn.gudqs7.plugins.savior.action.rpc;

import cn.gudqs7.plugins.savior.action.base.AbstractReqDocerSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;

/**
 * @author wq
 */
public class RpcReqDocSaviorAction extends AbstractReqDocerSavior {

    public RpcReqDocSaviorAction() {
        super(ThemeHelper.getRpcTheme());
    }
}
