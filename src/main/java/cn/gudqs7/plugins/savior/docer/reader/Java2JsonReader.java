package cn.gudqs7.plugins.savior.docer.reader;

import cn.gudqs7.plugins.savior.docer.reader.base.AbstractJsonReader;
import cn.gudqs7.plugins.savior.docer.theme.Theme;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WQ
 * @date 2022/4/4
 */
public class Java2JsonReader extends AbstractJsonReader<Map<String, Object>> {

    public Java2JsonReader(Theme theme) {
        super(theme);
    }

    @Override
    protected Map<String, Object> handleReturnNull() {
        return new HashMap<>(2);
    }

}
