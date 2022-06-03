package cn.gudqs7.plugins.savior.reader;

import cn.gudqs7.plugins.common.base.reader.AbstractReader;
import cn.gudqs7.plugins.common.enums.StructureType;
import cn.gudqs7.plugins.common.pojo.ReadOnlyMap;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.common.util.IndexIncrementUtil;
import cn.gudqs7.plugins.common.util.StringTool;
import cn.gudqs7.plugins.savior.pojo.FieldLevelInfo;
import cn.gudqs7.plugins.savior.pojo.FieldMemoInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author WQ
 * @date 2022/4/4
 */
public class Java2ApiReader extends AbstractReader<FieldMemoInfo, Map<String, List<FieldLevelInfo>>> {

    @Override
    protected void beforeRead(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data) {
        Map<String, List<FieldLevelInfo>> levelMap = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));
        data.put("levelMap", levelMap);
    }

    @Override
    protected Map<String, List<FieldLevelInfo>> afterRead(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data) {
        return getFromData(data, "levelMap");
    }

    @Override
    protected void beforeLoop(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> loopData, Map<String, Object> data, ReadOnlyMap parentData) {
        List<FieldMemoInfo> fieldList = new ArrayList<>();
        loopData.put("fieldList", fieldList);
        Integer type = structureAndCommentInfo.getType();
        CommentInfo commentInfo = structureAndCommentInfo.getCommentInfo();
        String clazzDesc = "";
        String clazzTypeName = "";
        switch (StructureType.of(type)) {
            case PSI_CLASS:
            case PSI_FIELD:
            case PSI_PARAM:
            case PSI_RETURN:
                clazzDesc = commentInfo.getValue("");
                clazzTypeName = structureAndCommentInfo.getOriginalFieldType();
                break;
            case PSI_PARAM_LIST:
                clazzTypeName = "Params";
                clazzDesc = "接口参数列表";
                break;
            default:
                break;
        }
        if (clazzTypeName != null) {
            clazzTypeName = StringTool.replaceMd(clazzTypeName);
        }
        clazzDesc = StringTool.replaceMd(clazzDesc);
        loopData.put("clazzTypeName", clazzTypeName);
        loopData.put("clazzDesc", clazzDesc);
    }

    @Override
    protected void beforeLoop0(StructureAndCommentInfo structureAndCommentInfo, StructureAndCommentInfo parentStructureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData) {

    }

    @Override
    protected void inLoop(StructureAndCommentInfo structureAndCommentInfo, FieldMemoInfo leafData, Map<String, Object> loopData, Map<String, Object> data, ReadOnlyMap parentData) {
        List<FieldMemoInfo> fieldList = getFromData(loopData, "fieldList");
        fieldList.add(leafData);
        loopData.put("level", structureAndCommentInfo.getLevel());
    }

    @Override
    protected void afterLoop(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData, Map<String, Object> loopData, FieldMemoInfo leafData, boolean leaf) {
        List<FieldMemoInfo> fieldList = getFromData(loopData, "fieldList");
        if (fieldList.size() > 0) {
            Map<String, List<FieldLevelInfo>> levelMap = getFromData(data, "levelMap");
            int level = (int) loopData.get("level");
            String clazzDesc = getFromData(loopData, "clazzDesc");
            String clazzTypeName = getFromData(loopData, "clazzTypeName");
            String parentClazzTypeName = parentData.get("clazzTypeName", "");
            FieldLevelInfo fieldLevelInfo = new FieldLevelInfo();
            fieldLevelInfo.setLevel(level);
            fieldLevelInfo.setParentClazzTypeName(parentClazzTypeName);
            fieldLevelInfo.setClazzTypeName(clazzTypeName);
            fieldLevelInfo.setClazzDesc(clazzDesc);
            fieldLevelInfo.setFieldList(fieldList);
            String levelStr = String.valueOf(level);
            List<FieldLevelInfo> list = levelMap.computeIfAbsent(levelStr, integer -> new ArrayList<>());
            list.add(fieldLevelInfo);
            levelMap.put(levelStr, list);
        }
    }

    @Override
    protected FieldMemoInfo readLeaf(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData) {
        String fieldName = structureAndCommentInfo.getFieldName();
        CommentInfo commentInfo = structureAndCommentInfo.getCommentInfo();
        if (commentInfo != null) {
            boolean required = commentInfo.isRequired(false);
            String fieldDesc = commentInfo.getValue("");
            String notes = commentInfo.getNotes("");
            fieldName = commentInfo.getName(fieldName);
            if (StringUtils.isNotBlank(fieldDesc)) {
                fieldDesc = StringTool.replaceMd(fieldDesc);
            }
            if (StringUtils.isNotBlank(notes)) {
                notes = StringTool.replaceMd(notes);
            }
            String fieldTypeName = structureAndCommentInfo.getFieldType();
            Integer fieldTypeCode = structureAndCommentInfo.getFieldTypeCode();
            String originalFieldType = structureAndCommentInfo.getOriginalFieldType();
            Integer originalFieldTypeCode = structureAndCommentInfo.getOriginalFieldTypeCode();
            if (StringUtils.isNotBlank(fieldTypeName)) {
                fieldTypeName = StringTool.replaceMd(fieldTypeName);
            }
            Integer level = structureAndCommentInfo.getLevel();

            int index = IndexIncrementUtil.getIndex();
            FieldMemoInfo fieldMemoInfo = new FieldMemoInfo();
            fieldMemoInfo.setIndex(index);
            fieldMemoInfo.setLevel(level);
            fieldMemoInfo.setFieldName(fieldName);
            fieldMemoInfo.setFieldTypeName(fieldTypeName);
            fieldMemoInfo.setOriginalFieldTypeName(originalFieldType);
            fieldMemoInfo.setFieldTypeCode(fieldTypeCode);
            fieldMemoInfo.setOriginalFieldTypeCode(originalFieldTypeCode);

            fieldMemoInfo.setRequired(required);
            fieldMemoInfo.setFieldDesc(fieldDesc);
            fieldMemoInfo.setNotes(notes);
            fieldMemoInfo.setLevelPrefix(getLevelStr(level));
            return fieldMemoInfo;
        }

        return null;
    }

    @Override
    protected Map<String, List<FieldLevelInfo>> handleReturnNull() {
        return new HashMap<>(2);
    }

    private String getLevelStr(int level) {
        if (level < 2) {
            return "";
        } else {
            StringBuilder str = new StringBuilder("└─");
            for (int i = 2; i < level; i++) {
                str.insert(0, "&ensp;&ensp;&ensp;&ensp;");
            }
            return str.toString();
        }
    }

}
