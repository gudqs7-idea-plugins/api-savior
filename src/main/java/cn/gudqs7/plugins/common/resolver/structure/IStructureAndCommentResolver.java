package cn.gudqs7.plugins.common.resolver.structure;

import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.impl.source.PsiClassReferenceType;

/**
 * @author WQ
 * @date 2022/4/4
 */
public interface IStructureAndCommentResolver {

    /**
     * 解析获取 psiClass 的注释/注解/结构信息
     *
     * @param psiClassReferenceType psiClass
     * @return psiClass 的注释/注解/结构信息
     */
    StructureAndCommentInfo resolveFromClass(PsiClassReferenceType psiClassReferenceType);

    /**
     * 解析获取参数的注释/注解/结构信息
     *
     * @param parameterList 参数
     * @return 参数的注释/注解/结构信息
     */
    StructureAndCommentInfo resolveFromParameterList(PsiParameterList parameterList);

    /**
     * 解析获取返回值的注释/注解/结构信息
     *
     * @param returnTypeElement 返回值
     * @return 返回值的注释/注解/结构信息
     */
    StructureAndCommentInfo resolveFromReturnVal(PsiTypeElement returnTypeElement);

}
