package cn.gudqs7.plugins.common.enums;


/**
 * @author WQ
 * @date 2022/4/5
 */
public enum FieldType {

    /**
     * 基础
     */
    BASE(1),

    /**
     * 用户自定义类
     */
    POJO(2),

    /**
     * 数组
     */
    ARRAY(3),

    /**
     * List(包括Set)
     */
    LIST(4),
    /**
     * Map
     */
    MAP(5),

    /**
     * 参数列表
     */
    PARAM_LIST(6),

    /**
     * 返回值
     */
    RETURN_VOID(7),

    /**
     * Set
     */
    SET(8),

    /**
     * Collection
     */
    COLLECTION(9),

    /**
     * MultipartFile (Spring MVC)
     */
    FILE(10),

    /**
     * 其他
     */
    OTHER(0);

    private final Integer type;

    FieldType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public static FieldType of(int type) {
        for (FieldType value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return FieldType.OTHER;
    }

}
