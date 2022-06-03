package cn.gudqs7.plugins.savior.reader;

import cn.gudqs7.plugins.savior.theme.Theme;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WQ
 * @date 2022/4/4
 */
public class Java2MapReader extends AbstractJsonReader<Map<String, Object>> {

    public Java2MapReader(Theme theme) {
        super(theme);
    }

    @Override
    protected Map<String, Object> handleReturnNull() {
        return new HashMap<>(2);
    }

}
