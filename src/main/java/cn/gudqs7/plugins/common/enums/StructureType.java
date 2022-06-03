package cn.gudqs7.plugins.common.enums;


/**
 * @author WQ
 * @date 2022/4/5
 */
public enum StructureType {

    /**
     * 1.类
     */
    PSI_CLASS(1),

    /**
     * 2.字段
     */
    PSI_FIELD(2),
    /**
     * 3.参数
     */
    PSI_PARAM(3),
    /**
     * 4.返回值
     */
    PSI_RETURN(4),

    /**
     * 5.方法(暂时用不到)
     */
    PSI_METHOD(5),
    /**
     * 6.参数列表
     */
    PSI_PARAM_LIST(6),

    /**
     * 其他
     */
    PSI_PARAM_OTHER(0);

    private final Integer type;

    StructureType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public static StructureType of(int type) {
        for (StructureType value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return StructureType.PSI_PARAM_OTHER;
    }

}
