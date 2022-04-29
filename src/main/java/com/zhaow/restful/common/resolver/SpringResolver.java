package com.zhaow.restful.common.resolver;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.zhaow.restful.annotations.PathMappingAnnotation;
import com.zhaow.restful.annotations.SpringControllerAnnotation;
import com.zhaow.restful.common.spring.RequestMappingAnnotationHelper;
import com.zhaow.restful.method.RequestPath;
import com.zhaow.restful.method.action.PropertiesHandler;
import com.zhaow.restful.navigation.action.RestServiceItem;

import java.util.*;

/**
 * @author restful
 */
public class SpringResolver extends BaseServiceResolver {

    PropertiesHandler propertiesHandler;

    public SpringResolver(Module module) {
        myModule = module;
        propertiesHandler = new PropertiesHandler(module);
    }

    public SpringResolver(Project project) {
        myProject = project;

    }


    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();

        SpringControllerAnnotation[] supportedAnnotations = SpringControllerAnnotation.values();
        Map<String, PsiClass> psiClassMap = new HashMap<>();
        for (PathMappingAnnotation controllerAnnotation : supportedAnnotations) {
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(controllerAnnotation.getShortName(), project, globalSearchScope);
            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
                PsiElement psiElement = psiModifierList.getParent();
                PsiClass psiClass = (PsiClass) psiElement;
                psiClassMap.put(psiClass.getQualifiedName(), psiClass);
            }
        }
        for (PsiClass psiClass : psiClassMap.values()) {
            List<RestServiceItem> serviceItemList = getServiceItemList(psiClass);
            itemList.addAll(serviceItemList);
        }

        return itemList;
    }

    protected List<RestServiceItem> getServiceItemList(PsiClass psiClass) {

        PsiMethod[] psiMethods = psiClass.getMethods();

        List<RestServiceItem> itemList = new ArrayList<>();
        List<RequestPath> classRequestPaths = RequestMappingAnnotationHelper.getRequestPaths(psiClass);

        for (PsiMethod psiMethod : psiMethods) {
            RequestPath[] methodRequestPaths = RequestMappingAnnotationHelper.getRequestPaths(psiMethod);

            if (classRequestPaths != null && classRequestPaths.size() > 0) {
                RequestPath classRequestPath = classRequestPaths.get(0);
                String path = classRequestPath.getPath();
                if (methodRequestPaths != null && methodRequestPaths.length > 0) {
                    RequestPath methodRequestPath = methodRequestPaths[0];
                    RestServiceItem item = createRestServiceItem(psiMethod, path, methodRequestPath);
                    itemList.add(item);
                }
            }

        }
        return itemList;
    }


}
