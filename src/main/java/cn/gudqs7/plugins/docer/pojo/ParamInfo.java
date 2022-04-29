package cn.gudqs7.plugins.docer.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author wq
 */
@Data
public class ParamInfo {

    private int index;
    private int level;
    private String en;
    private String cn;
    private List<ParamLineInfo> allFields;

}
