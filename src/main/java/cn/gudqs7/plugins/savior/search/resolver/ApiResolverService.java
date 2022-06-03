package cn.gudqs7.plugins.savior.search.resolver;

import cn.gudqs7.plugins.savior.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.savior.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.savior.docer.util.ActionUtil;
import cn.gudqs7.plugins.savior.util.PsiAnnotationUtil;
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
                ActionUtil.handleException(e);
            }
        }
        return navigationItemList;
    }

    protected List<ApiNavigationItem> getServiceItemList(PsiClass psiClass) {
        List<ApiNavigationItem> navigationItemList = new ArrayList<>(2);
        if (psiClass == null) {
            return navigationItemList;
        }
        Set<String> classPathSet = getClassRequestMappingPath(psiClass);
        List<MethodPathInfo> methodPathList = new ArrayList<>(32);

        PsiMethod[] psiMethods = psiClass.getMethods();
        for (PsiMethod psiMethod : psiMethods) {
            try {
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_MAPPING, null));
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_GET_MAPPING, HttpMethod.GET));
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_POST_MAPPING, HttpMethod.POST));
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_PUT_MAPPING, HttpMethod.PUT));
                methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_DELETE_MAPPING, HttpMethod.DELETE));
            } catch (Exception e) {
                ActionUtil.handleException(e);
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
    private List<MethodPathInfo> getMethodList0(PsiMethod psiMethod, String qnameOfMapping, HttpMethod httpMethod) {
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
                    String psiMethodName = psiMethod.getName();
                    String location = psiMethodName;
                    PsiClass psiClass = psiMethod.getContainingClass();
                    if (psiClass != null) {
                        String psiClassName = psiClass.getName();
                        location = psiClassName + "#" + psiMethodName;
                    }
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
