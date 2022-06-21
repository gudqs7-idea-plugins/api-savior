package cn.gudqs7.plugins.common.resolver.comment;

import cn.gudqs7.plugins.common.consts.CommonConst;
import cn.gudqs7.plugins.common.enums.CommentTagEnum;
import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfoTag;
import cn.gudqs7.plugins.common.pojo.resolver.RequestMapping;
import cn.gudqs7.plugins.common.pojo.resolver.ResponseCodeInfo;
import cn.gudqs7.plugins.common.util.WebEnvironmentUtil;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiAnnotationUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiTypeUtil;
import com.intellij.psi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author wq
 */
public class PsiMethodAnnotationHolderImpl extends AbstractAnnotationHolder {

    private final PsiMethod psiMethod;

    public PsiMethodAnnotationHolderImpl(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    @Override
    public PsiAnnotation getAnnotationByQname(String qName) {
        return psiMethod.getAnnotation(qName);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag commentInfoTag = new CommentInfoTag();
        for (PsiElement child : psiMethod.getChildren()) {
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
                        line = line.replaceAll("\\*", "").trim();
                        if (StringUtils.isBlank(line)) {
                            continue;
                        }
                        if (line.contains("@") || line.contains("#")) {
                            if (line.startsWith("@code") || line.startsWith("#code")) {
                                // remove @code itself
                                line = line.substring(5).trim();
                                String[] codeInfoArray = line.split(" ");
                                if (codeInfoArray.length > 0) {
                                    String code = codeInfoArray[0];
                                    String message = "";
                                    if (codeInfoArray.length > 1) {
                                        message = line.substring(code.length()).trim();
                                    }
                                    ResponseCodeInfo codeInfo = new ResponseCodeInfo(code, message);
                                    commentInfoTag.getResponseCodeInfoList().add(codeInfo);
                                }
                                continue;
                            }

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
                                switch (CommentTagEnum.of(tag)) {
                                    case HIDDEN:
                                        commentInfoTag.setHidden(getBooleanVal(tagVal));
                                        break;
                                    case IMPORTANT:
                                        commentInfoTag.setImportant(getBooleanVal(tagVal));
                                        break;
                                    case NOTES:
                                        commentInfoTag.appendNotes(tagVal);
                                        break;
                                    default:
                                        break;
                                }
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
        dealRequestMapping(commentInfoTag);
        return commentInfoTag;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasOperationAnnotation = hasAnnotation(QNAME_OF_OPERATION);
        if (hasOperationAnnotation) {
            commentInfo.setHidden(getAnnotationValueByApiOperation(CommentTagEnum.HIDDEN.getTag()));
            String value = getAnnotationValueByApiOperation(CommentTagEnum.DEFAULT.getTag());
            String notes = getAnnotationValueByApiOperation(CommentTagEnum.NOTES.getTag());
            if (StringUtils.isNotBlank(value)) {
                value = value.replaceAll("\\n", CommonConst.BREAK_LINE);
            }
            if (StringUtils.isNotBlank(notes)) {
                notes = notes.replaceAll("\\n", CommonConst.BREAK_LINE);
            }
            commentInfo.setValue(value);
            commentInfo.setNotes(notes);
        }
        // 添加 knife4j 的 ApiOperationSupport 注解支持, 主要是 includeParameters 和 ignoreParameters
        // 优先级是有 ignoreParameters 则跳过 includeParameters 字段
        if (hasAnnotation(QNAME_OF_OPERATION_SUPPORT)) {
            List<String> includeParameters = getAnnotationValueByQname(QNAME_OF_OPERATION_SUPPORT, "includeParameters");
            addRequestTagInfo(commentInfo, includeParameters, MoreCommentTagEnum.ONLY_REQUEST.getTag());
            List<String> ignoreParameters = getAnnotationValueByQname(QNAME_OF_OPERATION_SUPPORT, "ignoreParameters");
            addRequestTagInfo(commentInfo, ignoreParameters, MoreCommentTagEnum.HIDDEN_REQUEST.getTag());
        }
        if (hasAnnotation(QNAME_OF_RESPONSES)) {
            // 存在多个 code
            List<PsiAnnotation> psiAnnotationList = getAnnotationValueByQname(QNAME_OF_RESPONSES, "value");
            if (psiAnnotationList != null && psiAnnotationList.size() > 0) {
                for (PsiAnnotation psiAnnotation : psiAnnotationList) {
                    Integer code = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "code", null);
                    String message = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "message", null);
                    ResponseCodeInfo codeInfo = new ResponseCodeInfo(String.valueOf(code), message);
                    commentInfo.getResponseCodeInfoList().add(codeInfo);
                }
            }
        } else if (hasAnnotation(QNAME_OF_RESPONSE)) {
            // 存在单个 code
            Integer code = getAnnotationValueByQname(QNAME_OF_RESPONSE, "code");
            String message = getAnnotationValueByQname(QNAME_OF_RESPONSE, "message");
            ResponseCodeInfo codeInfo = new ResponseCodeInfo(String.valueOf(code), message);
            commentInfo.getResponseCodeInfoList().add(codeInfo);
        }
        dealRequestMapping(commentInfo);
        return commentInfo;
    }

    /**
     * 获取注解中的信息
     *
     * @param attr 注解字段
     * @return 信息
     */
    private <T> T getAnnotationValueByApiOperation(String attr) {
        return getAnnotationValueByQname(QNAME_OF_OPERATION, attr);
    }

    private void addRequestTagInfo(CommentInfo commentInfo, List<String> requestParameters, String tagKey) {
        if (CollectionUtils.isNotEmpty(requestParameters)) {
            Set<String> paramNameSet = new HashSet<>(8);
            PsiParameterList parameterList = psiMethod.getParameterList();
            if (!parameterList.isEmpty()) {
                PsiParameter[] parameters = parameterList.getParameters();
                for (PsiParameter parameter : parameters) {
                    paramNameSet.add(parameter.getName());
                }
            }
            List<String> paramList = new ArrayList<>();
            for (String requestParameter : requestParameters) {
                int indexOfPoint = requestParameter.indexOf(".");
                if (indexOfPoint != -1) {
                    String prefix = requestParameter.substring(0, indexOfPoint);
                    if (paramNameSet.contains(prefix)) {
                        paramList.add(requestParameter.substring(indexOfPoint + 1));
                    } else {
                        paramList.add(requestParameter);
                    }
                }
            }
            String request = String.join(",", paramList);
            commentInfo.appendToTag(tagKey, request);
        }
    }

    private void dealRequestMapping(CommentInfo commentInfo) {
        boolean hasMappingAnnotation = hasAnyOneAnnotation(QNAME_OF_MAPPING, QNAME_OF_GET_MAPPING, QNAME_OF_POST_MAPPING, QNAME_OF_PUT_MAPPING, QNAME_OF_DELETE_MAPPING);
        if (hasMappingAnnotation) {
            if (hasAnnotation(QNAME_OF_MAPPING)) {
                List<String> path = getAnnotationListValueByQname(QNAME_OF_MAPPING, "value");
                if (CollectionUtils.isEmpty(path)) {
                    path = getAnnotationListValueByQname(QNAME_OF_MAPPING, "path");
                }
                String path0 = "";
                if (CollectionUtils.isNotEmpty(path)) {
                    path0 = path.get(0);
                }
                commentInfo.setUrl(path0);
                String method;
                List<String> methodList = getAnnotationListValueByQname(QNAME_OF_MAPPING, "method");
                if (CollectionUtils.isEmpty(methodList)) {
                    method = "GET/POST/PUT/DELETE";
                } else {
                    method = String.join("/", methodList);
                }
                commentInfo.setMethod(method);
            }
            dealHttpMethod(commentInfo, QNAME_OF_POST_MAPPING, RequestMapping.Method.POST);
            dealHttpMethod(commentInfo, QNAME_OF_GET_MAPPING, RequestMapping.Method.GET);
            dealHttpMethod(commentInfo, QNAME_OF_PUT_MAPPING, RequestMapping.Method.PUT);
            dealHttpMethod(commentInfo, QNAME_OF_DELETE_MAPPING, RequestMapping.Method.DELETE);

            // deal controller RequestMapping
            String controllerUrl = "/";
            PsiClass containingClass = psiMethod.getContainingClass();
            if (containingClass == null) {
                ExceptionUtil.handleSyntaxError(psiMethod.getName() + "'s Class");
            }
            PsiAnnotation psiAnnotation = containingClass.getAnnotation(QNAME_OF_MAPPING);
            if (psiAnnotation != null) {
                List<String> pathList = PsiAnnotationUtil.getAnnotationListValue(psiAnnotation, "value", null);
                if (CollectionUtils.isEmpty(pathList)) {
                    pathList = PsiAnnotationUtil.getAnnotationListValue(psiAnnotation, "path", null);
                }
                if (CollectionUtils.isNotEmpty(pathList)) {
                    controllerUrl = pathList.get(0);
                }
                if (controllerUrl.startsWith("/")) {
                    controllerUrl = controllerUrl.substring(1);
                }
                if (!controllerUrl.endsWith("/")) {
                    controllerUrl = controllerUrl + "/";
                }
            }
            String hostPrefix = getHostPrefix();
            String nowUrl = commentInfo.getUrl("");
            if (nowUrl.startsWith("/")) {
                nowUrl = nowUrl.substring(1);
            }
            commentInfo.setUrl(hostPrefix + controllerUrl + nowUrl);
            for (PsiElement child : psiMethod.getChildren()) {
                if (child instanceof PsiParameterList) {
                    PsiParameterList parameterList = (PsiParameterList) child;
                    boolean parameterHasRequestBody = false;
                    boolean parameterHasFile = false;
                    if (!parameterList.isEmpty()) {
                        for (PsiParameter parameter : parameterList.getParameters()) {
                            PsiAnnotation annotation = parameter.getAnnotation(QNAME_OF_REQUEST_BODY);
                            if (annotation != null) {
                                parameterHasRequestBody = true;
                            }
                            PsiType psiType = parameter.getType();
                            if (PsiTypeUtil.isPsiTypeFromXxx(psiType, parameter.getProject(), QNAME_OF_MULTIPART_FILE)) {
                                parameterHasFile = true;
                            }
                        }
                    }
                    if (parameterHasRequestBody) {
                        commentInfo.setContentType(RequestMapping.ContentType.APPLICATION_JSON);
                    }
                    if (parameterHasFile) {
                        commentInfo.setContentType(RequestMapping.ContentType.FORM_DATA);
                    }
                    break;
                }
            }
        }
    }

    private String getHostPrefix() {
        String hostPrefix = "http://%s:%s/";
        String ip = WebEnvironmentUtil.getIp();
        String port = "8080";
        String portByConfigFile = WebEnvironmentUtil.getPortByConfigFile(psiMethod.getProject(), psiMethod.getContainingFile());
        if (StringUtils.isNotBlank(portByConfigFile)) {
            port = portByConfigFile;
        }
        return String.format(hostPrefix, ip, port);
    }

    private void dealHttpMethod(CommentInfo commentInfo, String qnameOfXxxMapping, String post) {
        if (hasAnnotation(qnameOfXxxMapping)) {
            List<String> path = getAnnotationListValueByQname(qnameOfXxxMapping, "value");
            if (CollectionUtils.isEmpty(path)) {
                path = getAnnotationListValueByQname(qnameOfXxxMapping, "path");
            }
            String path0 = "";
            if (CollectionUtils.isNotEmpty(path)) {
                path0 = path.get(0);
            }
            commentInfo.setUrl(path0);
            commentInfo.setMethod(post);
        }
    }

    @Override
    protected boolean usingAnnotation() {
        return hasAnnotation(QNAME_OF_OPERATION);
    }

}
