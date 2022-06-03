package cn.gudqs7.plugins.common.resolver.comment;

import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfoTag;
import com.intellij.psi.*;

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
    String QNAME_OF_JSON_FORMAT = "com.fasterxml.jackson.annotation.JsonFormat";
    String QNAME_OF_DATE_TIME_FORMAT = "org.springframework.format.annotation.DateTimeFormat";
    String QNAME_OF_VALID_NOT_NULL = "javax.validation.constraints.NotNull";
    String QNAME_OF_VALID_NOT_EMPTY = "org.hibernate.validator.constraints.NotEmpty";
    String QNAME_OF_VALID_NOT_BLANK = "org.hibernate.validator.constraints.NotBlank";
    String QNAME_OF_VALID_LENGTH = "org.hibernate.validator.constraints.Length";
    String QNAME_OF_VALID_MIN = "javax.validation.constraints.Min";
    String QNAME_OF_VALID_MAX = "javax.validation.constraints.Max";
    String QNAME_OF_VALID_RANGE = "org.hibernate.validator.constraints.Range";

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
     *
     * @param psiMethod
     * @return
     */
    static AnnotationHolder getPsiMethodHolder(PsiMethod psiMethod) {
        return new PsiMethodAnnotationHolderImpl(psiMethod);
    }

    /**
     * 根据 注解全限定名 获取注解
     *
     * @param qName 注解全限定名
     * @return 注解信息
     */
    PsiAnnotation getAnnotationByQname(String qName);

    /**
     * 根据注解全限定名判断是否带有此注解
     *
     * @param qName 注解全限定名
     * @return 是否带有此注解
     */
    boolean hasAnnotation(String qName);

    /**
     * 根据注解全限定名判断是否带有此任一一个注解
     * @param qNames 注解全限定名数组
     * @return 是否带有此任一一个注解
     */
    boolean hasAnyOneAnnotation(String... qNames);

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
