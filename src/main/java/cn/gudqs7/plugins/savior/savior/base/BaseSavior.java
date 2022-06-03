package cn.gudqs7.plugins.savior.savior.base;

import cn.gudqs7.plugins.savior.theme.Theme;

/**
 * @author wq
 */
public abstract class BaseSavior {

    protected Theme theme;

    public Theme getTheme() {
        return theme;
    }

    public BaseSavior(Theme theme) {
        this.theme = theme;
    }

}
