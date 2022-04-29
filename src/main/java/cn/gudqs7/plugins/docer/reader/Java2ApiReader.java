package cn.gudqs7.plugins.docer.reader;

import cn.gudqs7.plugins.docer.constant.StructureType;
import cn.gudqs7.plugins.docer.pojo.ParamInfo;
import cn.gudqs7.plugins.docer.pojo.ParamLineInfo;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.reader.base.AbstractReader;
import cn.gudqs7.plugins.docer.theme.Theme;
import cn.gudqs7.plugins.docer.util.IndexIncrementUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author WQ
 * @date 2022/4/4
 */
public class Java2ApiReader extends AbstractReader<ParamLineInfo, String> {

    public Java2ApiReader(Theme theme) {
        super(theme);
    }

    @Override
    protected void beforeRead(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data) {
        Map<Integer, List<ParamInfo>> goMap = new TreeMap<>(Comparator.comparingInt(o -> o));
        boolean returnType = structureAndCommentInfo.isReturnType();

        data.put("goMap", goMap);
        data.put("returnType", returnType);
    }

    @Override
    protected String afterRead(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data) {
        Map<Integer, List<ParamInfo>> goMap = getFromData(data, "goMap");
        return theme.printByGoMap(goMap, structureAndCommentInfo.isReturnType());
    }

    @Override
    protected void beforeLoop(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> loopData, Map<String, Object> data, Map<String, Object> parentData) {
        List<ParamLineInfo> levelOther = new ArrayList<>();
        loopData.put("levelOther", levelOther);
        Integer type = structureAndCommentInfo.getType();
        CommentInfo commentInfo = structureAndCommentInfo.getCommentInfo();
        String clazzDesc = "";
        String clazzTypeName = "";
        switch (StructureType.of(type)) {
            case PSI_CLASS:
            case PSI_FIELD:
            case PSI_RETURN:
                clazzDesc = commentInfo.getValue("");
                clazzTypeName = structureAndCommentInfo.getFieldType();
                break;
            case PSI_PARAM:
                clazzTypeName = "请求参数";
                break;
            case PSI_PARAM_LIST:
                clazzTypeName = "Params";
                clazzDesc = "接口参数列表";
                break;
            default:
                break;
        }
        loopData.put("clazzTypeName", clazzTypeName);
        loopData.put("clazzDesc", clazzDesc);
        parentData.put("parentClassType", clazzTypeName);
    }

    @Override
    protected void beforeLoop0(StructureAndCommentInfo structureAndCommentInfo, StructureAndCommentInfo parentStructureAndCommentInfo, Map<String, Object> data, Map<String, Object> parentData) {

    }

    @Override
    protected void inLoop(StructureAndCommentInfo structureAndCommentInfo, ParamLineInfo leafData, Map<String, Object> loopData, Map<String, Object> data, Map<String, Object> parentData) {
        List<ParamLineInfo> levelOther = getFromData(loopData, "levelOther");
        levelOther.add(leafData);
        loopData.put("level", structureAndCommentInfo.getLevel());
    }

    @Override
    protected void afterLoop(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, Map<String, Object> parentData, Map<String, Object> loopData, ParamLineInfo leafData, boolean leaf) {
        List<ParamLineInfo> levelOther = getFromData(loopData, "levelOther");
        if (levelOther.size() > 0) {
            Map<Integer, List<ParamInfo>> goMap = getFromData(data, "goMap");
            String clazzDesc = getFromData(loopData, "clazzDesc");
            String clazzTypeName = getFromData(loopData, "clazzTypeName");
            int level = (int) loopData.get("level");
            String parentClazzTypeName = getFromData(parentData, "parentClassType");
            boolean afterLevelTwo = level > 2;
            if (afterLevelTwo && parentClazzTypeName != null) {
                clazzTypeName = parentClazzTypeName + "=>" + clazzTypeName;
            }
            String addition = "";
            if (StringUtils.isNoneBlank(clazzDesc)) {
                addition = "（" + clazzDesc + "）";
            }
            ParamInfo paramInfo = new ParamInfo();
            int index = IndexIncrementUtil.getIndex();
            paramInfo.setLevel(level);
            paramInfo.setIndex(index);
            paramInfo.setEn(clazzTypeName);
            paramInfo.setCn(addition);
            paramInfo.setAllFields(levelOther);

            List<ParamInfo> list = goMap.computeIfAbsent(level, integer -> new ArrayList<>());
            list.add(paramInfo);
            goMap.put(level, list);
        }
    }

    @Override
    protected ParamLineInfo readLeaf(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, Map<String, Object> parentData) {
        boolean returnType = getFromData(data, "returnType");
        String fieldName = structureAndCommentInfo.getFieldName();
        CommentInfo commentInfo = structureAndCommentInfo.getCommentInfo();
        if (commentInfo != null) {
            boolean required = commentInfo.isRequired(false);
            String fieldDesc = commentInfo.getValue("");
            String notes = commentInfo.getNotes("");
            fieldName = commentInfo.getName(fieldName);
            if (StringUtils.isNotBlank(fieldDesc)) {
                fieldDesc = replaceMd(fieldDesc);
            }
            if (StringUtils.isNotBlank(notes)) {
                notes = replaceMd(notes);
            }
            String fieldTypeName = structureAndCommentInfo.getFieldType();
            if (StringUtils.isNotBlank(fieldTypeName)) {
                fieldTypeName = replaceMd(fieldTypeName);
            }
            String requiredStr = required ? "是" : "否";
            String requiredStrMarkdown = required ? "**是**" : "否";
            Integer level = structureAndCommentInfo.getLevel();

            Map<String, String> data0 = new HashMap<>(16);
            data0.put("fieldName", fieldName);
            data0.put("fieldType", fieldTypeName);
            data0.put("requiredStr", requiredStr);
            data0.put("requiredStrMarkdown", requiredStrMarkdown);
            data0.put("fieldDesc", fieldDesc);
            data0.put("notes", notes);
            data0.put("levelPrefix", getLevelStr(level));
            int index = IndexIncrementUtil.getIndex();
            String result = getTemplate(theme.getParamContentPath(returnType), data0);
            return new ParamLineInfo(index, result, level);
        }

        return null;
    }

    @Override
    protected String handleReturnNull() {
        return "";
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
