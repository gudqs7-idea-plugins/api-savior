package cn.gudqs7.plugins.idea.pojo;

import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 */
public class JsonExampleInfo implements CharSequence {

    private Object realVal;

    private FieldExampleInfo fieldExampleInfo;

    public JsonExampleInfo() {
    }

    public JsonExampleInfo(Object realVal, FieldExampleInfo fieldExampleInfo) {
        this.realVal = realVal;
        this.fieldExampleInfo = fieldExampleInfo;
    }

    public Object getRealVal() {
        return realVal;
    }

    public void setRealVal(Object realVal) {
        this.realVal = realVal;
    }

    public FieldExampleInfo getFieldExampleInfo() {
        return fieldExampleInfo;
    }

    public void setFieldExampleInfo(FieldExampleInfo fieldExampleInfo) {
        this.fieldExampleInfo = fieldExampleInfo;
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    @Override
    public @NotNull String toString() {
        if (realVal == null) {
            return "null";
        }
        return realVal.toString();
    }
}
