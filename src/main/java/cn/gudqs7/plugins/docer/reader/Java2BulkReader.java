package cn.gudqs7.plugins.docer.reader;

import cn.gudqs7.plugins.docer.constant.CommentConst;
import cn.gudqs7.plugins.docer.constant.FieldType;
import cn.gudqs7.plugins.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.docer.pojo.PostmanKvInfo;
import cn.gudqs7.plugins.docer.pojo.ReadOnlyMap;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.reader.base.AbstractJsonReader;
import cn.gudqs7.plugins.docer.theme.Theme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author WQ
 * @date 2022/4/4
 */
public class Java2BulkReader extends AbstractJsonReader<List<PostmanKvInfo>> {

    public static final String FINAL_DEFAULT_OBJECT_EXAMPLE = "{}";

    public Java2BulkReader(Theme theme) {
        super(theme);
    }

    @Override
    protected List<PostmanKvInfo> afterRead(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data) {
        Map<String, Object> map = getFromData(data, "map");
        boolean removeRequestBody = getFromData(data, "removeRequestBody", false);
        if (removeRequestBody) {
            Object key = map.remove(MapKeyConstant.HAS_REQUEST_BODY);
            if (key != null) {
                map.remove(key.toString());
            }
        }
        return getBulkList(map, "");
    }

    @Override
    protected List<PostmanKvInfo> handleReturnNull() {
        return null;
    }

    private List<PostmanKvInfo> getBulkList(Map<String, Object> json, String prefix) {
        if (json == null || json.size() == 0) {
            return new ArrayList<>();
        }
        List<PostmanKvInfo> kvList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            handleKeyValue(prefix, kvList, key, value);
        }
        return kvList;
    }

    private void handleKeyValue(String prefix, List<PostmanKvInfo> kvList, String key, Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            if (map == null || map.size() == 0) {
                kvList.add(kv(prefix + key, FINAL_DEFAULT_OBJECT_EXAMPLE, "", true));
            } else {
                List<PostmanKvInfo> mapKvList = getBulkList(map, prefix + key + ".");
                kvList.addAll(mapKvList);
            }
        } else if (value instanceof List) {
            List list = (List) value;
            for (int i = 0; i < list.size(); i++) {
                Object obj = list.get(i);
                handleKeyValue(prefix, kvList, key + "[" + i + "]", obj);
            }
        } else if (value instanceof Object[]) {
            Object[] list = (Object[]) value;
            for (int i = 0; i < list.length; i++) {
                Object obj = list[i];
                handleKeyValue(prefix, kvList, key + "[" + i + "]", obj);
            }
        } else {
            if (value instanceof StructureAndCommentInfo) {
                StructureAndCommentInfo structureAndCommentInfo = (StructureAndCommentInfo) value;
                Integer fieldTypeCode = structureAndCommentInfo.getFieldTypeCode();
                String originalFieldType = structureAndCommentInfo.getOriginalFieldType();
                CommentInfo commentInfo = structureAndCommentInfo.getCommentInfo();
                String desc = "";
                String example = FINAL_DEFAULT_OBJECT_EXAMPLE;
                boolean required = false;
                if (commentInfo != null) {
                    desc = commentInfo.getValue(desc);
                    desc = desc.replaceAll(CommentConst.BREAK_LINE, "\n");
                    Object exampleVal = getBaseExampleVal(originalFieldType, commentInfo);
                    if (exampleVal != null) {
                        example = String.valueOf(exampleVal);
                    }
                    required = commentInfo.isRequired(false);
                }
                if (FieldType.FILE.getType().equals(fieldTypeCode)) {
                    kvList.add(kvFile(prefix + key, example, desc, !required));
                } else {
                    kvList.add(kv(prefix + key, example, desc, !required));
                }
            } else if (value == null || value.getClass() == Object.class) {
                kvList.add(kv(prefix + key, FINAL_DEFAULT_OBJECT_EXAMPLE, "", true));
            }
        }
    }

    private PostmanKvInfo kv(String key, String value, String desc, boolean disabled) {
        PostmanKvInfo postmanKvInfo = new PostmanKvInfo();
        postmanKvInfo.setKey(key);
        postmanKvInfo.setValue(value);
        postmanKvInfo.setDescription(desc);
        postmanKvInfo.setType("text");
        postmanKvInfo.setDisabled(disabled);
        return postmanKvInfo;
    }

    private PostmanKvInfo kvFile(String key, String example, String desc, boolean disabled) {
        if (StringUtils.isBlank(example)) {
            example = "/" + desc + ".txt";
        }
        String projectPath = "project";
        if (project != null) {
            projectPath = project.getBasePath();
        }

        String os = System.getProperty("os.name");
        String home = System.getProperty("user.home");
        boolean windows = os != null && os.contains("Windows");
        if (windows) {
            example = "/" + example;
            home = home.replaceAll("\\\\", "/");
        }
        String desktopPath = home + "/Desktop";
        String downloadPath = home + "/Downloads";
        example = example.replaceAll("\\$\\{desktop}", desktopPath);
        example = example.replaceAll("\\$\\{download}", downloadPath);
        example = example.replaceAll("\\$\\{project}", projectPath);
        PostmanKvInfo postmanKvInfo = new PostmanKvInfo();
        postmanKvInfo.setKey(key);
        postmanKvInfo.setSrc(example);
        postmanKvInfo.setDescription(desc);
        postmanKvInfo.setType("file");
        postmanKvInfo.setDisabled(disabled);
        return postmanKvInfo;
    }

    @Override
    protected Object getJsonMapVal(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData) {
        return structureAndCommentInfo;
    }
}
