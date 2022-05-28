package cn.gudqs7.plugins.search.resolver;

import cn.gudqs7.plugins.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.docer.savior.base.BaseSavior;
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
            navigationItemList.addAll(getServiceItemList(psiClass));
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
            methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_MAPPING, null));
            methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_GET_MAPPING, HttpMethod.GET));
            methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_POST_MAPPING, HttpMethod.POST));
            methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_PUT_MAPPING, HttpMethod.PUT));
            methodPathList.addAll(getMethodList0(psiMethod, AnnotationHolder.QNAME_OF_DELETE_MAPPING, HttpMethod.DELETE));
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
                navigationItemList.add(new ApiNavigationItem(psiMethod, httpMethod, fullPath));
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
                List<String> methodList = BaseSavior.getAnnotationListValue(methodMappingAnnotation, "method", null);
                if (CollectionUtils.isNotEmpty(methodList)) {
                    httpMethod = HttpMethod.of(methodList.get(0));
                }
            }
            if (CollectionUtils.isNotEmpty(pathList)) {
                for (String methodPath : pathList) {
                    methodPathList.add(new MethodPathInfo(psiMethod, httpMethod, methodPath));
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
        List<String> pathList = BaseSavior.getAnnotationListValue(requestMappingAnnotation, "path", null);
        if (CollectionUtils.isEmpty(pathList)) {
            pathList = BaseSavior.getAnnotationListValue(requestMappingAnnotation, "value", null);
        }
        return pathList;
    }


}
