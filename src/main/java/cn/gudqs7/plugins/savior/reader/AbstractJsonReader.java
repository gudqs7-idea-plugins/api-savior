package cn.gudqs7.plugins.savior.reader;

import cn.gudqs7.plugins.common.base.reader.AbstractMapReader;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.savior.theme.Theme;

import java.util.Map;

/**
 * @author wq
 * @date 2022/6/3
 */
public abstract class AbstractJsonReader<T> extends AbstractMapReader<T> {

    private final Theme theme;

    public Theme getTheme() {
        return theme;
    }

    public AbstractJsonReader(Theme theme) {
        this.theme = theme;
    }

    @Override
    protected boolean needBreakUpParam(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> map, String fieldName) {
        return theme.handleParameter(structureAndCommentInfo, map, fieldName);
    }

}
