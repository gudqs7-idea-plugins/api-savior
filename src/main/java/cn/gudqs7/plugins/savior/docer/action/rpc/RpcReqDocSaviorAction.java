package cn.gudqs7.plugins.savior.docer.action.rpc;

import cn.gudqs7.plugins.savior.docer.action.base.AbstractReqDocerSavior;
import cn.gudqs7.plugins.savior.docer.theme.ThemeHelper;

/**
 * @author wq
 */
public class RpcReqDocSaviorAction extends AbstractReqDocerSavior {

    public RpcReqDocSaviorAction() {
        super(ThemeHelper.getRpcTheme());
    }
}
