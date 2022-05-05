package cn.gudqs7.plugins.docer.pojo;

import lombok.Data;

/**
 * @author wq
 */
@Data
public class FieldMemoInfo {

    /**
     * 按解析顺序自增, 用于排序
     */
    private int index;

    /**
     * 当前层级(可用于过滤)
     */
    private int level;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段类型
     */
    private String fieldTypeName;

    /**
     * 原始字段类型
     */
    private String originalFieldTypeName;


    /**
     * 当前类型code
     *
     * @see cn.gudqs7.plugins.docer.constant.FieldType
     */
    private Integer fieldTypeCode;

    /**
     * 原始当前类型code
     *
     * @see cn.gudqs7.plugins.docer.constant.FieldType
     */
    private Integer originalFieldTypeCode;

    /**
     * 是否要求必填
     */
    private Boolean required;

    /**
     * 字段描述
     */
    private String fieldDesc;

    /**
     * 其他参考信息
     */
    private String notes;

    /**
     * 根据层级生成的前缀 用于修饰层级
     */
    private String levelPrefix;

}
