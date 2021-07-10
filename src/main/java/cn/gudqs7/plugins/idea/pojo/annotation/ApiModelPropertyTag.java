package cn.gudqs7.plugins.idea.pojo.annotation;

/**
 * @author wq
 */
public class ApiModelPropertyTag extends ApiModelProperty{
    /**
     * 是否更重要
     */
    private boolean important = false;

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }
}
