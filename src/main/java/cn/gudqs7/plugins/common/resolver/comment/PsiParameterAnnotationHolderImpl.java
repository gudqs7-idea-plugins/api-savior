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
                            line = processCommentLine(line);
                            if (line == null) {
                                continue;
                            }

                            if (line.startsWith("@param")) {
                                processParamTag(line, parameterName, commentTagMap, moreCommentTagMap, commentInfoTag);
                            } else {
                                commentInfoTag.appendValue(line, CommonConst.SPACE);
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

    private String processCommentLine(String line) {
        // 去除注释前缀并修剪空白字符
        line = removeJavaDocPrefix(line).trim();
        if (StringUtils.isBlank(line)) {
            return null; // 如果行为空或仅包含空白字符，返回 null
        }
        return line;
    }

    private void processParamTag(String line, String parameterName,
                                 Map<String, CommentTagEnum> commentTagMap,
                                 Map<String, MoreCommentTagEnum> moreCommentTagMap,
                                 CommentInfoTag commentInfoTag) {
        // 去掉 "@param" 前缀
        line = line.substring("@param".length()).trim();
        // 分割参数名和注释内容
        String[] paramInfoArray = line.split(" ", 2);
        if (paramInfoArray.length < 2) {
            return; // 格式错误，跳过
        }

        String fieldName = paramInfoArray[0];
        if (!fieldName.equals(parameterName)) {
            return; // 不是当前参数，跳过
        }

        // 获取注释内容部分
        String tagContent = paramInfoArray[1];
        // 分割注释内容中的标签
        String[] tagArray = tagContent.split(" ");
        for (String tagEntry : tagArray) {
            String tagName = tagEntry;
            String tagVal = null;

            // 处理键值对形式的标签
            if (tagEntry.contains("=")) {
                String[] tagKeyValue = tagEntry.split("=", 2);
                tagName = tagKeyValue[0];
                if (tagKeyValue.length > 1) {
                    tagVal = tagKeyValue[1];
                }
            }

            // 根据标签类型处理
            if (commentTagMap.containsKey(tagName)) {
                setCommentInfoByTag(commentInfoTag, tagName, tagVal);
            } else if (moreCommentTagMap.containsKey(tagName)) {
                commentInfoTag.appendToTag(tagName, tagVal);
            } else {
                commentInfoTag.appendValue(tagEntry, CommonConst.SPACE);
            }
        }
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
