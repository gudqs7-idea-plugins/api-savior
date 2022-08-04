package cn.gudqs7.plugins.savior.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author wq
 */
@Data
public class FieldLevelInfo {

    /**
     * 层级
     */
    private int level;

    /**
     * 父类型名称
     */
    private String parentClazzTypeName;

    /**
     * 当前类型名称
     */
    private String clazzTypeName;

    /**
     * 当前类描述
     */
    private String clazzDesc;

    /**
     * 当前类全限定名
     */
    private String clazzQname;

    /**
     * 当前层级下所有字段数据
     */
    private List<FieldMemoInfo> fieldList;

}
