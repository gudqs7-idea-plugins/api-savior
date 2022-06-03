package cn.gudqs7.plugins.savior.docer.reader;

import cn.gudqs7.plugins.common.base.reader.AbstractJsonReader;
import cn.gudqs7.plugins.common.pojo.ReadOnlyMap;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.savior.docer.pojo.ComplexInfo;
import cn.gudqs7.plugins.savior.docer.pojo.FieldCommentInfo;
import cn.gudqs7.plugins.savior.docer.theme.Theme;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WQ
 * @date 2022/4/4
 */
public class Java2ComplexReader extends AbstractJsonReader<Map<String, Object>> {

    public Java2ComplexReader(Theme theme) {
        super(theme);
    }

    @Override
    protected Map<String, Object> handleReturnNull() {
        return new HashMap<>(2);
    }

    @Override
    protected Object getJsonMapVal(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData) {
        CommentInfo commentInfo = structureAndCommentInfo.getCommentInfo();
        FieldCommentInfo fieldCommentInfo = new FieldCommentInfo();
        fieldCommentInfo.setFieldDesc(commentInfo.getValue(""));
        fieldCommentInfo.setExample(commentInfo.getExample(""));
        fieldCommentInfo.setRequired(commentInfo.isRequired(false));

        ComplexInfo complexInfo = new ComplexInfo();
        complexInfo.setRealVal(super.getJsonMapVal(structureAndCommentInfo, data, parentData));
        complexInfo.setFieldCommentInfo(fieldCommentInfo);
        complexInfo.setCommentInfo(commentInfo);

        return complexInfo;
    }
}
