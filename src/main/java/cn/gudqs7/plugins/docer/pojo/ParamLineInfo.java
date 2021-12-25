package cn.gudqs7.plugins.docer.pojo;

/**
 * @author wq
 */
public class ParamLineInfo {

    private int index;
    private int level;
    private String line;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public ParamLineInfo(int index, String line) {
        this.index = index;
        this.line = line;
    }

    public ParamLineInfo(int index, String line, int level) {
        this.index = index;
        this.line = line;
        this.level = level;
    }
}
