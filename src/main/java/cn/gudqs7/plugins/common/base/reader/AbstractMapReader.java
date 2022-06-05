package cn.gudqs7.plugins.common.base.reader;

import cn.gudqs7.plugins.common.enums.FieldType;
import cn.gudqs7.plugins.common.enums.StructureType;
import cn.gudqs7.plugins.common.pojo.ReadOnlyMap;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.structure.BaseTypeUtil;
import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WQ
 * @date 2022/4/4
 */
public abstract class AbstractMapReader<B> extends AbstractReader<Map<String, Object>, B> implements IStructureAndCommentReader<B> {

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
            needBreakUpParam = needBreakUpParam(structureAndCommentInfo, map, fieldName);
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
                    } else if (val instanceof Map) {
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
                map.putAll(leafData);
            }
        }
    }

    /**
     * 若方法参数类型是pojo, 是否拆分 pojo 的字段到 map 中
     * 主要在于 Spring MVC 绑定方法参数时, 忽视方法参数的前缀, 因此需要拆分
     * 另: 可在此方法中进行一些与方法参数有关的处理
     *
     * @param structureAndCommentInfo 结构信息(含注释)
     * @param map                     最终 map
     * @param fieldName               方法参数变量名
     * @return true: 拆分 | false:不拆分
     */
    protected abstract boolean needBreakUpParam(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> map, String fieldName);

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
                    List<Object> list = new ArrayList<>();
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
        Object baseExampleVal = getBaseExampleVal(structureAndCommentInfo);
        if (baseExampleVal == null) {
            baseExampleVal = new Object();
        }
        return baseExampleVal;
    }

    public Object getBaseExampleVal(StructureAndCommentInfo structureAndCommentInfo) {
        CommentInfo commentInfo = structureAndCommentInfo.getCommentInfo();
        try {
            Integer fieldTypeCode = structureAndCommentInfo.getOriginalFieldTypeCode();
            PsiType psiType = structureAndCommentInfo.getPsiType();
            if (FieldType.BASE.getType().equals(fieldTypeCode)) {
                return BaseTypeUtil.getBaseDefaultVal(psiType, commentInfo);
            } else {
                String qName = psiType.getCanonicalText();
                return BaseTypeUtil.getInterfaceDefaultVal(qName, commentInfo);
            }
        } catch (Exception e) {
            String fieldName = structureAndCommentInfo.getFieldName();
            String example = commentInfo.getExample("[未设置]");
            String errMsg = String.format("获取字段示例值时出错, 可能是字段类型转换失败! 字段名称: %s; 您设置的示例值:%s; ",
                    fieldName, example);
            ExceptionUtil.logException(e, errMsg);
            return null;
        }
    }

}
