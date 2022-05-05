package cn.gudqs7.plugins.docer.reader.base;

import cn.gudqs7.plugins.docer.constant.CommentConst;
import cn.gudqs7.plugins.docer.constant.FieldType;
import cn.gudqs7.plugins.docer.constant.StructureType;
import cn.gudqs7.plugins.docer.pojo.ReadOnlyMap;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.theme.Theme;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author WQ
 * @date 2022/4/4
 */
public abstract class AbstractJsonReader<B> extends AbstractReader<Map<String, Object>, B> implements IStructureAndCommentReader<B> {

    public AbstractJsonReader(Theme theme) {
        super(theme);
    }

    @Override
    protected void beforeRead(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data) {
        data.put("map", new LinkedHashMap<>(32));
    }

    @Override
    protected B afterRead(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data) {
        return getFromData(data, "map");
    }

    @Override
    protected void beforeLoop(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> loopData, Map<String, Object> data, ReadOnlyMap parentData) {
        LinkedHashMap<Object, Object> loopObj = new LinkedHashMap<>(32);
        loopData.put("loopObj", loopObj);
    }

    @Override
    protected void beforeLoop0(StructureAndCommentInfo structureAndCommentInfo, StructureAndCommentInfo parentStructureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData) {

    }

    @Override
    protected void inLoop(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> leafData, Map<String, Object> loopData, Map<String, Object> data, ReadOnlyMap parentData) {
        if (structureAndCommentInfo != null && structureAndCommentInfo.isLeaf()) {
            Boolean onlyRequire = (Boolean) data.get("onlyRequire");
            if (onlyRequire != null && onlyRequire) {
                // 要求必须必填才放入
                boolean required = structureAndCommentInfo.getCommentInfo().isRequired(false);
                if (!required) {
                    return;
                }
            }
        }
        Map<String, Object> loopObj = getFromData(loopData, "loopObj");
        loopObj.putAll(leafData);
    }

    @Override
    protected void afterLoop(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData, Map<String, Object> loopData, Map<String, Object> leafData, boolean leaf) {
        Map<String, Object> map = getFromData(data, "map");
        Map<String, Object> loopObj = getFromData(loopData, "loopObj");
        String fieldName = structureAndCommentInfo.getFieldName();
        Integer type = structureAndCommentInfo.getType();

        boolean needBreakUpParam = false;
        if (StructureType.PSI_PARAM.getType().equals(type)) {
            needBreakUpParam = theme.handleParameter(structureAndCommentInfo, map, fieldName);
        }

        if (leaf) {
            // 处理只有一个节点且为普通类型时的情况(即 read0 中 leaf=true 分支中的 afterLoop 进入时)
            map.putAll(loopObj);
            return;
        } else {
            // leafData 代表循环外的节点数据, 可称为父节点, 一般包含一对 key/value; 节点名称为 key, value 一般为 null 或预设值(参考 readLeaf中 switch)
            // loopObj 代表父节点的 children 所有数据, 一般为多对 key/value, 子节点名称为 key, value 可能是普通类型, 也可能是 Map/List (取决于 子节点是否还有子节点)
            // 这里判断是否需要覆盖父节点 (leafData) 的数据(因为 readLeaf 时不会直接返回数据)
            if (fieldName != null) {
                if (leafData.containsKey(fieldName)) {
                    Object val = leafData.get(fieldName);
                    if (val instanceof List) {
                        List<Object> list = (List<Object>) val;
                        ArrayList<Object> copyList = new ArrayList<>(list);
                        list.clear();
                        for (Object ignored : copyList) {
                            list.add(loopObj);
                        }
                        leafData.put(fieldName, list);
                    } else if (val instanceof Map){
                        Map<String, Object> valMap = (Map<String, Object>) val;
                        valMap.replaceAll((k, v) -> loopObj);
                        leafData.put(fieldName, valMap);
                    } else {
                        if (needBreakUpParam) {
                            leafData.remove(fieldName);
                            leafData.putAll(loopObj);
                        } else {
                            leafData.put(fieldName, loopObj);
                        }
                    }
                } else {
                    map.put(fieldName, loopObj);
                }
            } else {
                // 没有 fieldName, 没法 put 单个, 只好 putAll
                map.putAll(loopObj);
            }
        }

        // 单个参数/单个返回值处理
        if (structureAndCommentInfo.getParent() == null) {
            // 当单个返回值为pojo时, leafData 数据赋值成功, 但 map 仍为空, 需要添加到 map 中
            if (StructureType.of(type) == StructureType.PSI_RETURN) {
                map.putAll(loopObj);
            }
        }
    }

    @Override
    protected Map<String, Object> readLeaf(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData) {
        String fieldName = structureAndCommentInfo.getFieldName();
        if (fieldName != null) {
            Map<String, Object> map = new LinkedHashMap<>();
            Integer fieldTypeCode = structureAndCommentInfo.getFieldTypeCode();
            switch (FieldType.of(fieldTypeCode)) {
                case BASE:
                case FILE:
                    map.put(fieldName, getJsonMapVal(structureAndCommentInfo, data, parentData));
                    break;
                case POJO:
                    // 不管 put 什么, 之后都会在 afterLoop 中覆盖掉, 故直接放 null
                    map.put(fieldName, null);
                    break;
                case MAP:
                    Map<String, Object> valMap = new LinkedHashMap<>(4);
                    valMap.put("key1", getJsonMapVal(structureAndCommentInfo, data, parentData));
                    valMap.put("key2", getJsonMapVal(structureAndCommentInfo, data, parentData));
                    map.put(fieldName, valMap);
                    break;
                case ARRAY:
                case LIST:
                case SET:
                case COLLECTION:
                    ArrayList<Object> list = new ArrayList<>();
                    list.add(getJsonMapVal(structureAndCommentInfo, data, parentData));
                    list.add(getJsonMapVal(structureAndCommentInfo, data, parentData));
                    map.put(fieldName, list);
                    break;
                default:
                    break;
            }
            return map;
        }
        return null;
    }

    /**
     * 获取JsonMap结构中具体的 Val
     *
     * @param structureAndCommentInfo 结构信息+注释/注解信息
     * @param data                    全局数据
     * @param parentData              父数据
     * @return JsonMap结构中具体的 Val
     */
    protected Object getJsonMapVal(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data, ReadOnlyMap parentData) {
        String originalFieldType = structureAndCommentInfo.getOriginalFieldType();
        CommentInfo commentInfo = structureAndCommentInfo.getCommentInfo();
        Object baseExampleVal = getBaseExampleVal(originalFieldType, commentInfo);
        if (baseExampleVal == null) {
            baseExampleVal = new Object();
        }
        return baseExampleVal;
    }

    public Object getBaseExampleVal(String fieldType, CommentInfo commentInfo) {
        String example = commentInfo.getExample("");
        boolean noExampleValue = StringUtils.isBlank(example);
        Object paramValue = null;
        try {
            switch (fieldType.toLowerCase()) {
                case "byte":
                    paramValue = noExampleValue ? RandomUtils.nextBytes(1)[0] : Byte.parseByte(example);
                    break;
                case "char":
                case "character":
                    paramValue = noExampleValue ? randomChar() : example.charAt(0);
                    break;
                case "boolean":
                    paramValue = noExampleValue ? RandomUtils.nextBoolean() : Boolean.parseBoolean(example);
                    break;
                case "int":
                case "integer":
                    paramValue = noExampleValue ? RandomUtils.nextInt(1, 128) : Integer.parseInt(example);
                    break;
                case "double":
                    paramValue = noExampleValue ? RandomUtils.nextDouble(50, 1000) : Double.parseDouble(example);
                    break;
                case "float":
                    paramValue = noExampleValue ? RandomUtils.nextFloat(10, 100) : Float.parseFloat(example);
                    break;
                case "long":
                    paramValue = noExampleValue ? RandomUtils.nextLong(10, 1000) : Long.parseLong(example);
                    break;
                case "short":
                    paramValue = noExampleValue ? randomShort() : Short.parseShort(example);
                    break;
                case "number":
                    paramValue = 0;
                    break;
                case "bigdecimal":
                    paramValue = noExampleValue ? BigDecimal.valueOf(RandomUtils.nextDouble(50, 1000)) : new BigDecimal(example);
                    break;
                case "string":
                case "multipartfile":
                    paramValue = noExampleValue ? randomString(commentInfo) : example;
                    break;
                case "date":
                    paramValue = noExampleValue ? randomDate() : example;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            paramValue = example;
        }
        return paramValue;
    }

    private String randomDate() {
        Date now = new Date();
        now.setTime(System.currentTimeMillis() + RandomUtils.nextLong(0, 86400000));
        return DateFormatUtils.formatUTC(now, "yyyy-MM-dd'T'HH:mm:ss.SSS+0000");
    }

    private String randomString(CommentInfo commentInfo) {
        String fieldDesc = commentInfo.getValue("");
        Boolean random = commentInfo.getSingleBool("random", false);
        Boolean guid = commentInfo.getSingleBool("guid", false);
        if (guid) {
            return UUID.randomUUID().toString().toUpperCase();
        }
        if (random || StringUtils.isBlank(fieldDesc)) {
            int length = RandomUtils.nextInt(5, 20);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                stringBuilder.append(randomChar(false));
            }
            return stringBuilder.toString();
        } else {
            if (fieldDesc.contains(CommentConst.BREAK_LINE)) {
                fieldDesc = fieldDesc.substring(0, fieldDesc.indexOf(CommentConst.BREAK_LINE));
            }
            return fieldDesc + RandomUtils.nextInt(1, 128);
        }
    }

    private short randomShort() {
        return (short) RandomUtils.nextInt(1, 100);
    }

    private char randomChar() {
        boolean en = RandomUtils.nextBoolean();
        return randomChar(en);
    }

    private char randomChar(boolean en) {
        if (en) {
            boolean upper = RandomUtils.nextBoolean();
            if (upper) {
                return (char) RandomUtils.nextInt(65, 90);
            } else {
                return (char) RandomUtils.nextInt(97, 122);
            }
        } else {
            String str = "";
            int highCode;
            int lowCode;

            Random random = new Random();

            //B0 + 0~39(16~55) 一级汉字所占区
            highCode = (176 + Math.abs(random.nextInt(39)));
            //A1 + 0~93 每区有94个汉字
            lowCode = (161 + Math.abs(random.nextInt(93)));

            byte[] b = new byte[2];
            b[0] = (Integer.valueOf(highCode)).byteValue();
            b[1] = (Integer.valueOf(lowCode)).byteValue();

            try {
                str = new String(b, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return str.charAt(0);
        }
    }


}
