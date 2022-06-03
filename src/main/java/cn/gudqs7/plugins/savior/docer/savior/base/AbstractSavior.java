package cn.gudqs7.plugins.savior.docer.savior.base;

import cn.gudqs7.plugins.savior.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.savior.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.savior.docer.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.savior.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.savior.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.savior.docer.reader.Java2ApiReader;
import cn.gudqs7.plugins.savior.docer.reader.Java2JsonReader;
import cn.gudqs7.plugins.savior.docer.resolver.StructureAndCommentResolver;
import cn.gudqs7.plugins.savior.docer.theme.Theme;
import cn.gudqs7.plugins.savior.docer.util.DataHolder;
import cn.gudqs7.plugins.savior.util.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 * @date 2021/5/19
 */
public abstract class AbstractSavior<T> extends BaseSavior {

    protected final Java2JsonReader java2JsonReader;
    protected final Java2ApiReader java2ApiReader;
    protected final StructureAndCommentResolver structureAndCommentResolver;

    public AbstractSavior(Theme theme) {
        super(theme);
        java2JsonReader = new Java2JsonReader(theme);
        java2ApiReader = new Java2ApiReader(theme);
        structureAndCommentResolver = new StructureAndCommentResolver(theme);
    }

    protected int orderByMethod(PsiMethod publicMethod, PsiMethod publicMethod2) {
        PsiAnnotation orderAnnotation = publicMethod.getAnnotation("org.springframework.core.annotation.Order");
        int order = Integer.MAX_VALUE;
        if (orderAnnotation != null) {
            order = getAnnotationValue(orderAnnotation, "value", order);
        }
        PsiAnnotation orderAnnotation2 = publicMethod2.getAnnotation("org.springframework.core.annotation.Order");
        int order2 = Integer.MAX_VALUE;
        if (orderAnnotation2 != null) {
            order2 = getAnnotationValue(orderAnnotation2, "value", order2);
        }
        return order - order2;
    }

    protected boolean filterMethod(PsiMethod method) {
        if (method.isConstructor()) {
            return true;
        }
        PsiModifierList modifierList = method.getModifierList();
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            return true;
        }
        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            return true;
        }
        // 最后排除 Lombok 生成的方法
        return "LombokLightModifierList".equals(modifierList.toString());
    }

    /**
     * 获取方法上的ActionName注释
     *
     * @param method 方法
     * @return 方法上的ActionName注释
     */
    protected String getMethodActionName(PsiMethod method) {
        AnnotationHolder psiMethodHolder = AnnotationHolder.getPsiMethodHolder(method);
        CommentInfo propertyForMethod = psiMethodHolder.getCommentInfo();
        if (propertyForMethod != null) {
            return propertyForMethod.getSingleStr(MoreCommentTagEnum.AMP_ACTION_NAME.getTag(), null);
        }
        return null;
    }

    /**
     * 根据 PsiMethod 获取想要的数据
     *
     * @param project            项目
     * @param publicMethod       方法
     * @param interfaceClassName 接口类名
     * @param jumpHidden         是否跳过 hidden 检查
     * @return 想要的数据
     */
    protected T getDataByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, boolean jumpHidden) {
        return getDataByMethod(project, interfaceClassName, publicMethod, new HashMap<>(2), jumpHidden);
    }


    /**
     * 根据 PsiMethod 获取想要的数据
     *
     * @param project            项目
     * @param publicMethod       方法
     * @param interfaceClassName 接口类名
     * @param param              配置参数
     * @param jumpHidden         是否跳过 hidden 检查
     * @return 想要的数据
     */
    protected T getDataByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, Map<String, Object> param, boolean jumpHidden) {
        AnnotationHolder annotationHolder = AnnotationHolder.getPsiMethodHolder(publicMethod);
        CommentInfo commentInfo = annotationHolder.getCommentInfo();

        if (!jumpHidden) {
            boolean hidden = commentInfo.isHidden(false);
            if (hidden || theme.handleMethodHidden(annotationHolder)) {
                return null;
            }
        }
        structureAndCommentResolver.setProject(project);

        List<String> hiddenRequest = commentInfo.getHiddenRequest();
        List<String> onlyRequest = commentInfo.getOnlyRequest();
        DataHolder.addData(MapKeyConstant.HIDDEN_KEYS, hiddenRequest);
        DataHolder.addData(MapKeyConstant.ONLY_KEYS, onlyRequest);

        PsiParameterList parameterTypes = publicMethod.getParameterList();
        StructureAndCommentInfo paramStructureAndCommentInfo = structureAndCommentResolver.resolveFromParameterList(parameterTypes);

        DataHolder.removeAll();

        List<String> hiddenResponse = commentInfo.getHiddenResponse();
        List<String> onlyResponse = commentInfo.getOnlyResponse();
        DataHolder.addData(MapKeyConstant.HIDDEN_KEYS, hiddenResponse);
        DataHolder.addData(MapKeyConstant.ONLY_KEYS, onlyResponse);

        PsiTypeElement returnTypeElement = publicMethod.getReturnTypeElement();
        StructureAndCommentInfo returnStructureAndCommentInfo = structureAndCommentResolver.resolveFromReturnVal(returnTypeElement);

        DataHolder.removeAll();

        T data = getDataByStructureAndCommentInfo(
                project, publicMethod, commentInfo, interfaceClassName,
                paramStructureAndCommentInfo, returnStructureAndCommentInfo, param
        );

        PsiUtil.clearGeneric();
        return data;
    }

    /**
     * 根据参数/返回值信息获取想要的数据
     *
     * @param project                       项目
     * @param publicMethod                  方法
     * @param commentInfo                   方法注释/注解信息
     * @param interfaceClassName            接口类名
     * @param paramStructureAndCommentInfo  参数注释+结构信息
     * @param returnStructureAndCommentInfo 返回值注释+结构信息
     * @param param                         配置参数
     * @return 想要的数据
     */
    protected abstract T getDataByStructureAndCommentInfo(
            Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName,
            StructureAndCommentInfo paramStructureAndCommentInfo,
            StructureAndCommentInfo returnStructureAndCommentInfo,
            Map<String, Object> param);

}
