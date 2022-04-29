package cn.gudqs7.plugins.docer.pojo;

import lombok.Data;

/**
 * @author wq
 */
@Data
public class ParamLineInfo {

    private int index;
    private int level;
    private String line;

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
