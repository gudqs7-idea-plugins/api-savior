package cn.gudqs7.plugins.search.resolver;

import cn.gudqs7.plugins.common.enums.HttpMethod;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.resolver.comment.AnnotationHolder;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiAnnotationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author wq
 * @date 2022/5/28
 */
public class ApiResolverService {

    private final Project project;

    public ApiResolverService(Project project) {
        this.project = project;
    }

    public static ApiResolverService getInstance(Project project) {
        return new ApiResolverService(project);
    }

    @NotNull
    public List<ApiNavigationItem> getApiNavigationItemList() {
        List<ApiNavigationItem> navigationItemList = new ArrayList<>();
        String[] supportAnnotations = new String[]{"Controller", "RestController"};
        Map<String, PsiClass> psiClassMap = new HashMap<>();
        for (String supportAnnotation : supportAnnotations) {
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(supportAnnotation, project, GlobalSearchScope.projectScope(project));
            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
                PsiElement psiElement = psiModifierList.getParent();
                PsiClass psiClass = (PsiClass) psiElement;
                psiClassMap.put(psiClass.getQualifiedName(), psiClass);
            }
        }
        for (PsiClass psiClass : psiClassMap.values()) {
            try {
                navigationItemList.addAll(getServiceItemList(psiClass));
            } catch (Exception e) {
                String classQname = psiClass.getQualifiedName();
                ExceptionUtil.logException(e, String.format("扫描接口时出错, 接口类全限定名为: %s; 错误信息为: %s", classQname, e.getMessage()));
            }
        }
        return navigationItemList;
    }

    protected List<ApiNavigationItem> getServiceItemList(@NotNull PsiClass psiClass) {
        List<ApiNavigationItem> navigationItemList = new ArrayList<>(2);
        Set<String> classPathSet = getClassRequestMappingPath(psiClass);
        List<MethodPathInfo> methodPathList = new ArrayList<>(32);

        String classQname = psiClass.getQualifiedName();
        PsiMethod[] psiMethods = psiClass.getMethods();
        for (PsiMethod psiMethod : psiMethods) {
            try {
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_MAPPING, null, psiClass));
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_GET_MAPPING, HttpMethod.GET, psiClass));
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_POST_MAPPING, HttpMethod.POST, psiClass));
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_PUT_MAPPING, HttpMethod.PUT, psiClass));
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_DELETE_MAPPING, HttpMethod.DELETE, psiClass));
            } catch (Exception e) {
                String methodName = psiMethod.getName();
                String methodQname = classQname + "#" + methodName;
                ExceptionUtil.logException(e, String.format("扫描接口时出错, 方法为: %s; 错误信息为: %s", methodQname, e.getMessage()));
            }
        }

        for (String classPath : classPathSet) {
            for (MethodPathInfo methodPathInfo : methodPathList) {
                PsiMethod psiMethod = methodPathInfo.getPsiMethod();
                HttpMethod httpMethod = methodPathInfo.getHttpMethod();
                String methodPath = methodPathInfo.getMethodPath();

                if (!classPath.startsWith("/")) {
                    classPath = "/".concat(classPath);
                }
                if (!classPath.endsWith("/")) {
                    classPath = classPath.concat("/");
                }
                if (methodPath.startsWith("/")) {
                    methodPath = methodPath.substring(1);
                }
                String fullPath = classPath + methodPath;
                navigationItemList.add(new ApiNavigationItem(psiMethod, httpMethod, fullPath, methodPathInfo));
            }
        }
        return navigationItemList;
    }

    @NotNull
    private List<MethodPathInfo> getMethodList0(@NotNull PsiMethod psiMethod, String qnameOfMapping, HttpMethod httpMethod, PsiClass psiClass) {
        List<MethodPathInfo> methodPathList = new ArrayList<>(8);
        PsiAnnotation methodMappingAnnotation = psiMethod.getAnnotation(qnameOfMapping);
        if (methodMappingAnnotation != null) {
            List<String> pathList = getRequestMappingPath(methodMappingAnnotation);
            if (httpMethod == null) {
                List<String> methodList = PsiAnnotationUtil.getAnnotationListValue(methodMappingAnnotation, "method", null);
                if (CollectionUtils.isNotEmpty(methodList)) {
                    httpMethod = HttpMethod.of(methodList.get(0));
                } else {
                    httpMethod = HttpMethod.GET;
                }
            }
            if (CollectionUtils.isNotEmpty(pathList)) {
                for (String methodPath : pathList) {
                    String psiClassName = psiClass.getName();
                    String psiMethodName = psiMethod.getName();
                    String location = psiClassName + "#" + psiMethodName;
                    AnnotationHolder psiMethodHolder = AnnotationHolder.getPsiMethodHolder(psiMethod);
                    CommentInfo commentInfo = psiMethodHolder.getCommentInfo();
                    String description = commentInfo.getValue("");
                    methodPathList.add(new MethodPathInfo(psiMethod, httpMethod, methodPath, location, description));
                }
            }
        }
        return methodPathList;
    }

    @NotNull
    private Set<String> getClassRequestMappingPath(PsiClass psiClass) {
        if (psiClass == null) {
            return new HashSet<>(2);
        }
        Set<String> classPathSet = new HashSet<>(8);
        PsiAnnotation requestMappingAnnotation = psiClass.getAnnotation(AnnotationHolder.QNAME_OF_MAPPING);
        if (requestMappingAnnotation != null) {
            List<String> pathList = getRequestMappingPath(requestMappingAnnotation);
            if (CollectionUtils.isEmpty(pathList)) {
                classPathSet.add("/");
            } else {
                classPathSet.addAll(pathList);
            }
        }
        return classPathSet;
    }

    @Nullable
    private List<String> getRequestMappingPath(PsiAnnotation requestMappingAnnotation) {
        List<String> pathList = PsiAnnotationUtil.getAnnotationListValue(requestMappingAnnotation, "path", null);
        if (CollectionUtils.isEmpty(pathList)) {
            pathList = PsiAnnotationUtil.getAnnotationListValue(requestMappingAnnotation, "value", null);
        }
        return pathList;
    }


}
