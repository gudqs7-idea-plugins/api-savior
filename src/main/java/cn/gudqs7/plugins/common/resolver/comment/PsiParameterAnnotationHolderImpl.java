package cn.gudqs7.plugins.common.resolver.comment;

import cn.gudqs7.plugins.common.consts.CommonConst;
import cn.gudqs7.plugins.common.enums.CommentTagEnum;
import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfoTag;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author wq
 */
public class PsiParameterAnnotationHolderImpl extends AbstractFieldAnnotationHolder {

    private final PsiParameter psiParameter;

    public PsiParameterAnnotationHolderImpl(PsiParameter psiParameter) {
        this.psiParameter = psiParameter;
    }

    @Override
    public PsiAnnotation getAnnotationByQname(String qName) {
        return psiParameter.getAnnotation(qName);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag commentInfoTag = new CommentInfoTag();
        PsiElement parent = psiParameter.getParent().getParent();
        String parameterName = psiParameter.getName();
        if (parent instanceof PsiMethod) {
            for (PsiElement child : parent.getChildren()) {
                if (child instanceof PsiDocComment) {
                    Map<String, CommentTagEnum> commentTagMap = CommentTagEnum.allTagMap();
                    Map<String, MoreCommentTagEnum> moreCommentTagMap = MoreCommentTagEnum.allTagMap();
                    PsiDocComment psiComment = (PsiDocComment) child;
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
                            if (line.contains("@")) {
                                String atParam = "@param";
                                if (line.startsWith("@param")) {
                                    line = line.substring(atParam.length()).trim();
                                    String[] paramInfoArray = line.split(" ");
                                    String fieldName = "";
                                    if (paramInfoArray.length > 0) {
                                        fieldName = paramInfoArray[0];
                                    }
                                    if (!fieldName.equals(parameterName)) {
                                        continue;
                                    }
                                    String tag = line.substring(fieldName.length()).trim();
                                    String[] tagArray = tag.split(" ");
                                    for (String t : tagArray) {
                                        String tagName = t;
                                        String tagVal = null;
                                        if (t.contains("=")) {
                                            String[] tagKeyVal = t.split("=");
                                            tagName = tagKeyVal[0];
                                            if (tagKeyVal.length > 1) {
                                                tagVal = tagKeyVal[1];
                                            }
                                        }
                                        if (commentTagMap.containsKey(tagName)) {
                                            setCommentInfoByTag(commentInfoTag, tagName, tagVal);
                                        } else if (moreCommentTagMap.containsKey(tagName)) {
                                            commentInfoTag.appendToTag(tagName, tagVal);
                                        } else {
                                            commentInfoTag.appendValue(t, CommonConst.SPACE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        dealOtherAnnotation(commentInfoTag);
        return commentInfoTag;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasParamAnnotatation = hasAnnotation(QNAME_OF_PARAM);
        if (hasParamAnnotatation) {
            commentInfo.setHidden(getAnnotationValueByParam(CommentTagEnum.HIDDEN.getTag()));
            commentInfo.setRequired(getAnnotationValueByParam(CommentTagEnum.REQUIRED.getTag()));
            commentInfo.setValue(getAnnotationValueByParam(CommentTagEnum.DEFAULT.getTag()));
            commentInfo.setExample(getAnnotationValueByParam(CommentTagEnum.EXAMPLE.getTag()));
        }
        dealOtherAnnotation(commentInfo);
        return commentInfo;
    }


    private void dealOtherAnnotation(CommentInfo commentInfo) {
        boolean hasReqParamAnnotation = hasAnnotation(QNAME_OF_REQ_PARAM);
        if (hasReqParamAnnotation) {
            String name = getAnnotationValueByReqParam("name");
            if (name == null) {
                name = getAnnotationValueByReqParam(CommentTagEnum.DEFAULT.getTag());
            }
            commentInfo.setName(name);
            Boolean required = getAnnotationValueByReqParam(CommentTagEnum.REQUIRED.getTag());
            if (required == null || required) {
                commentInfo.setRequired(true);
            }
        }
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
    private <T> T getAnnotationValueByReqParam(String attr) {
        return getAnnotationValueByQname(QNAME_OF_REQ_PARAM, attr);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr 注解字段
     * @return 信息
     */
    protected <T> T getAnnotationValueByParam(String attr) {
        return getAnnotationValueByQname(QNAME_OF_PARAM, attr);
    }


    @Override
    protected boolean usingAnnotation() {
        return hasAnyOneAnnotation(QNAME_OF_PARAM);
    }
}
