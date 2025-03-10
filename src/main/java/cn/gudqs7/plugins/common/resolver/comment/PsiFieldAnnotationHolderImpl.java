package cn.gudqs7.plugins.common.resolver.comment;

import cn.gudqs7.plugins.common.consts.CommonConst;
import cn.gudqs7.plugins.common.enums.CommentTagEnum;
import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfoTag;
import cn.gudqs7.plugins.common.util.structure.PsiAnnotationUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author wq
 */
public class PsiFieldAnnotationHolderImpl extends AbstractFieldAnnotationHolder {

    private final PsiField psiField;

    public PsiFieldAnnotationHolderImpl(PsiField psiField) {
        this.psiField = psiField;
    }

    @Override
    public PsiAnnotation getAnnotationByQname(String qName) {
        return psiField.getAnnotation(qName);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag commentInfoTag = new CommentInfoTag();
        for (PsiElement child : psiField.getChildren()) {
            if (child instanceof PsiComment) {
                Map<String, CommentTagEnum> commentTagMap = CommentTagEnum.allTagMap();
                Map<String, MoreCommentTagEnum> moreCommentTagMap = MoreCommentTagEnum.allTagMap();
                PsiComment psiComment = (PsiComment) child;
                String text = psiComment.getText();
                if (text.startsWith("/**") && text.endsWith("*/")) {
                    String[] lines = text.replaceAll("\r", "").split("\n");
                    for (String line : lines) {
                        if (line.contains("/**") || line.contains("*/")) {
                            continue;
                        }
                        line = removeJavaDocPrefix(line);
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
                            if (commentTagMap.containsKey(tag)) {
                                setCommentInfoByTag(commentInfoTag, tag, tagVal);
                            } else if (moreCommentTagMap.containsKey(tag)) {
                                commentInfoTag.appendToTag(tag, tagVal);
                            }
                        } else {
                            commentInfoTag.appendValue(line);
                        }
                    }
                }
                break;
            }
        }
        dealOtherAnnotation(commentInfoTag);
        return commentInfoTag;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        CommentInfo commentInfo = new CommentInfo();
        PsiAnnotation psiAnnotation = getAnnotationByQname(QNAME_OF_PROPERTY);
        if (psiAnnotation != null) {
            commentInfo.setHidden(getAnnotationValueByProperty(psiAnnotation,CommentTagEnum.HIDDEN.getTag()));
            commentInfo.setRequired(getAnnotationValueByProperty(psiAnnotation,CommentTagEnum.REQUIRED.getTag()));
            String value = getAnnotationValueByProperty(psiAnnotation,CommentTagEnum.DEFAULT.getTag());
            String notes = getAnnotationValueByProperty(psiAnnotation,CommentTagEnum.NOTES.getTag());
            if (StringUtils.isNotBlank(value)) {
                value = value.replaceAll("\\n", CommonConst.BREAK_LINE);
            }
            if (StringUtils.isNotBlank(notes)) {
                notes = notes.replaceAll("\\n", CommonConst.BREAK_LINE);
            }
            commentInfo.setValue(value);
            commentInfo.setNotes(notes);
            commentInfo.setExample(getAnnotationValueByProperty(psiAnnotation,CommentTagEnum.EXAMPLE.getTag()));
        }
        psiAnnotation = getAnnotationByQname(QNAME_OF_OPENAPI_SCHEMA);
        if (psiAnnotation != null) {
            // 补充 CommentInfo 中已有的属性
            commentInfo.setHidden(getAnnotationValueByProperty(psiAnnotation, "hidden"));
            commentInfo.setRequired(getAnnotationValueByProperty(psiAnnotation, "required"));
            commentInfo.setValue(getAnnotationValueByProperty(psiAnnotation, "description"));
            commentInfo.setExample(getAnnotationValueByProperty(psiAnnotation, "example"));
        }
        dealOtherAnnotation(commentInfo);
        return commentInfo;
    }

    private void dealOtherAnnotation(CommentInfo commentInfo) {
        // 处理日期注解
        handleDateFormatAnnotation(commentInfo);
        // 根据 @Valid 配置信息覆盖是否必填字段
        overrideRequiredByValid(commentInfo);
        // 往更多说明填充一些信息
        addInfoToNotes(commentInfo);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr 注解字段
     * @return 信息
     */
    private  <T> T getAnnotationValueByProperty(String attr) {
        return getAnnotationValueByQname(QNAME_OF_PROPERTY, attr);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr 注解字段
     * @return 信息
     */
    private  <T> T getAnnotationValueByProperty( PsiAnnotation psiAnnotation,String attr) {
        return PsiAnnotationUtil.getAnnotationValue(psiAnnotation, attr, null);
    }

    @Override
    protected boolean usingAnnotation() {
        return hasAnyOneAnnotation(QNAME_OF_PROPERTY, QNAME_OF_OPENAPI_SCHEMA);
    }

}
