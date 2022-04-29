package cn.gudqs7.plugins.docer.annotation;

import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfoTag;
import cn.gudqs7.plugins.docer.savior.base.BaseSavior;
import cn.gudqs7.plugins.docer.util.BaseTypeParseUtil;
import com.intellij.psi.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解/注释获取类
 * @author wq
 */
public interface AnnotationHolder {

    String QNAME_OF_API = "io.swagger.annotations.Api";
    String QNAME_OF_MODEL = "io.swagger.annotations.ApiModel";
    String QNAME_OF_PROPERTY = "io.swagger.annotations.ApiModelProperty";
    String QNAME_OF_OPERATION = "io.swagger.annotations.ApiOperation";
    String QNAME_OF_OPERATION_SUPPORT = "com.github.xiaoymin.knife4j.annotations.ApiOperationSupport";
    String QNAME_OF_PARAM = "io.swagger.annotations.ApiParam";
    String QNAME_OF_RESPONSE = "io.swagger.annotations.ApiResponse";
    String QNAME_OF_RESPONSES = "io.swagger.annotations.ApiResponses";
    String QNAME_OF_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    String QNAME_OF_GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    String QNAME_OF_POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    String QNAME_OF_PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    String QNAME_OF_DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    String QNAME_OF_REQ_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    String QNAME_OF_REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    String QNAME_OF_MULTIPART_FILE = "org.springframework.web.multipart.MultipartFile";


    /**
     * 获取注解
     *
     * @param qname
     * @return
     */
    PsiAnnotation getAnnotation(String qname);

    /**
     * 是否带有某注解
     *
     * @param qname
     * @return
     */
    default boolean hasAnnotation(String qname) {
        return getAnnotation(qname) != null;
    }

    /**
     * 是否带有某些注解其中任意一个
     *
     * @param qnames
     * @return
     */
    default boolean hasAnyOneAnnotation(String... qnames) {
        for (String qname : qnames) {
            boolean hasAnnotation = getAnnotation(qname) != null;
            if (hasAnnotation) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取注解中的信息
     *
     * @param attr       注解字段
     * @return 信息
     */
    default <T> T getAnnotationValueByProperty(String attr) {
        return getAnnotationValueByQname(QNAME_OF_PROPERTY, attr);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr       注解字段
     * @return 信息
     */
    default <T> T getAnnotationValueByParam(String attr) {
        return getAnnotationValueByQname(QNAME_OF_PARAM, attr);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr       注解字段
     * @return 信息
     */
    default <T> T getAnnotationValueByReqParam(String attr) {
        return getAnnotationValueByQname(QNAME_OF_REQ_PARAM, attr);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr       注解字段
     * @return 信息
     */
    default <T> T getAnnotationValueByModel(String attr) {
        return getAnnotationValueByQname(QNAME_OF_MODEL, attr);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr       注解字段
     * @return 信息
     */
    default <T> T getAnnotationValueByOperation(String attr) {
        return getAnnotationValueByQname(QNAME_OF_OPERATION, attr);
    }

    /**
     * 获取注解中的信息
     *
     * @param qname      指定注解
     * @param attr       注解字段
     * @return 信息
     */
    default <T> T getAnnotationValueByQname(String qname, String attr) {
        PsiAnnotation psiAnnotation = getAnnotation(qname);
        return BaseSavior.getAnnotationValue(psiAnnotation, attr, null);
    }

    default <T> List<T> getAnnotationListValueByQname(String qname, String attr) {
        Object annotationValueByQname = getAnnotationValueByQname(qname, attr);
        if (annotationValueByQname == null) {
            return null;
        }
        if (annotationValueByQname instanceof List) {
            return (List<T>) annotationValueByQname;
        } else {
            List<T> list = new ArrayList<>();
            list.add((T) annotationValueByQname);
            return list;
        }
    }

    /**
     * 获取 flag, 不指定 false 则认为是 true
     *
     * @param tagVal
     * @return flag
     */
    default boolean getBooleanVal(String tagVal) {
        boolean flag;
        if (StringUtils.isBlank(tagVal)) {
            flag = true;
        } else {
            flag = BaseTypeParseUtil.parseBoolean(tagVal, true);
        }
        return flag;
    }


    /**
     * 获取字段的 holder
     *
     * @param psiField
     * @return
     */
    static AnnotationHolder getPsiFieldHolder(PsiField psiField) {
        return new PsiFieldAnnotationHolderImpl(psiField);
    }

    /**
     * 获取参数的 holder
     *
     * @param psiParameter
     * @return
     */
    static AnnotationHolder getPsiParameterHolder(PsiParameter psiParameter) {
        return new PsiParameterAnnotationHolderImpl(psiParameter);
    }

    /**
     * 获取类相关的holder
     * @param psiClass
     * @return
     */
    static AnnotationHolder getPsiClassHolder(PsiClass psiClass) {
        return new PsiClassAnnotationHolderImpl(psiClass);
    }

    /**
     * 获取方法返回值相关的 holder
     * @param returnTypeElement
     * @return
     */
    static AnnotationHolder getPsiReturnTypeHolder(PsiTypeElement returnTypeElement) {
        return new PsiReturnTypeAnnotationHolderImpl(returnTypeElement);
    }

    /**
     * 获取方法相关的 holder
     * @param psiMethod
     * @return
     */
    static AnnotationHolder getPsiMethodHolder(PsiMethod psiMethod) {
        return new PsiMethodAnnotationHolderImpl(psiMethod);
    }

    /**
     * 根据注释获取所需信息
     *
     * @return 所需信息
     */
    CommentInfoTag getCommentInfoByComment();

    /**
     * 根据注解获取所需信息
     *
     * @return 所需信息
     */
    CommentInfo getCommentInfoByAnnotation();

    /**
     * 综合注释/注解返回所需信息
     *
     * @return 所需信息
     */
    CommentInfo getCommentInfo();
}
