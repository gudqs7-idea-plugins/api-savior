package cn.gudqs7.plugins.docer.pojo;

import java.util.List;

/**
 * @author wq
 */
public class ParamInfo {

    private int index;
    private int level;
    private String en;
    private String cn;
    private List<ParamLineInfo> allFields;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public List<ParamLineInfo> getAllFields() {
        return allFields;
    }

    public void setAllFields(List<ParamLineInfo> allFields) {
        this.allFields = allFields;
    }
}
