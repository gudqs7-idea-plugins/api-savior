package cn.gudqs7.plugins.docer.annotation;

import cn.gudqs7.plugins.docer.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.docer.pojo.annotation.ApiModelPropertyTag;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentTag;
import cn.gudqs7.plugins.docer.util.AnnotationHolder;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wq
 */
public class PsiFieldAnnotationHolderImpl implements AnnotationHolder {

    private PsiField psiField;

    public PsiFieldAnnotationHolderImpl(PsiField psiField) {
        this.psiField = psiField;
    }

    @Override
    public PsiAnnotation getAnnotation(String qname) {
        return psiField.getAnnotation(qname);
    }

    @Override
    public ApiModelPropertyTag getApiModelPropertyByComment() {
        ApiModelPropertyTag apiModelPropertyTag = new ApiModelPropertyTag();
        for (PsiElement child : psiField.getChildren()) {
            if (child instanceof PsiComment) {
                PsiComment psiComment = (PsiComment) child;
                String text = psiComment.getText();
                if (text.startsWith("/**") && text.endsWith("*/")) {
                    String[] lines = text.replaceAll("\r", "").split("\n");
                    for (String line : lines) {
                        if (line.contains("/**") || line.contains("*/")) {
                            continue;
                        }
                        line = line.replaceAll("\\*", "").trim();
                        if (StringUtils.isBlank(line)) {
                            continue;
                        }
                        if (line.contains("@") || line.contains("#")) {
                            String[] tagValArray = line.split(" ");
                            String tag = "";
                            String tagVal = null;
                            if (tagValArray.length > 0) {
                                tag = tagValArray[0].trim();
                            }
                            if (tagValArray.length > 1) {
                                tagVal = line.substring(tag.length()).trim();
                            }
                            tag = tag.substring(1);
                            switch (tag) {
                                case CommentTag.REQUIRED:
                                    apiModelPropertyTag.setRequired(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.HIDDEN:
                                    apiModelPropertyTag.setHidden(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.IMPORTANT:
                                    apiModelPropertyTag.setImportant(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.EXAMPLE:
                                    apiModelPropertyTag.setExample(tagVal);
                                    break;
                                case CommentTag.NOTES:
                                    String notes = apiModelPropertyTag.getNotes(null);
                                    if (notes != null) {
                                        apiModelPropertyTag.setNotes(notes + "&br;" + tagVal);
                                    } else {
                                        apiModelPropertyTag.setNotes(tagVal);
                                    }
                                    break;
                                default:
                                    List<String> list = apiModelPropertyTag.getOtherTagMap().computeIfAbsent(tag, k -> new ArrayList<>());
                                    list.add(tagVal);
                                    break;
                            }
                        } else {
                            String oldValue = apiModelPropertyTag.getValue(null);
                            if (oldValue != null) {
                                apiModelPropertyTag.setValue(oldValue + "&br;" + line);
                            } else {
                                apiModelPropertyTag.setValue(line);
                            }
                        }
                    }
                }
                break;
            }
        }
        return apiModelPropertyTag;
    }

    @Override
    public ApiModelProperty getApiModelPropertyByAnnotation() {
        ApiModelProperty apiModelProperty = new ApiModelProperty();
        boolean hasAnnotatation = hasAnnotatation(QNAME_OF_PROPERTY);
        if (hasAnnotatation) {
            apiModelProperty.setHidden(getAnnotationValueByProperty("hidden"));
            apiModelProperty.setRequired(getAnnotationValueByProperty("required"));
            String value = getAnnotationValueByProperty("value");
            String notes = getAnnotationValueByProperty("notes");
            if (StringUtils.isNotBlank(value)) {
                value = value.replaceAll("\\n", "&br;");
            }
            if (StringUtils.isNotBlank(notes)) {
                notes = notes.replaceAll("\\n", "&br;");
            }
            apiModelProperty.setValue(value);
            apiModelProperty.setNotes(notes);
            apiModelProperty.setExample(getAnnotationValueByProperty("example"));
        }
        return apiModelProperty;
    }

    @Override
    public ApiModelProperty getApiModelProperty() {
        ApiModelProperty apiModelProperty = new ApiModelProperty();
        boolean hasAnnotatation = hasAnnotatation(QNAME_OF_PROPERTY);
        ApiModelPropertyTag apiModelPropertyByComment = getApiModelPropertyByComment();
        if (hasAnnotatation) {
            if (apiModelPropertyByComment.isImportant()) {
                apiModelProperty = apiModelPropertyByComment;
            } else {
                apiModelProperty = getApiModelPropertyByAnnotation();
                // 即使使用注解, 附加注释也会生效
                apiModelProperty.setOtherTagMap(apiModelPropertyByComment.getOtherTagMap());
            }
        } else {
            apiModelProperty = apiModelPropertyByComment;
        }
        return apiModelProperty;
    }

}
