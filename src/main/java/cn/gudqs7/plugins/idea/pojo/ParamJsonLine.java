package cn.gudqs7.plugins.idea.pojo;

import cn.gudqs7.plugins.idea.util.AnnotationHolder;

/**
 * @author wq
 */
public class ParamJsonLine {

    private String name;

    private String value;

    private AnnotationHolder annotationHolder;

    public ParamJsonLine(String name, String value, AnnotationHolder annotationHolder) {
        this.name = name;
        this.value = value;
        this.annotationHolder = annotationHolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AnnotationHolder getAnnotationHolder() {
        return annotationHolder;
    }

    public void setAnnotationHolder(AnnotationHolder annotationHolder) {
        this.annotationHolder = annotationHolder;
    }
}
