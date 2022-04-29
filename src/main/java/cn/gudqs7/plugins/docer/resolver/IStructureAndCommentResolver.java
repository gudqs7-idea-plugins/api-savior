package cn.gudqs7.plugins.docer.resolver;

import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.impl.source.PsiClassReferenceType;

/**
 * @author WQ
 * @date 2022/4/4
 */
public interface IStructureAndCommentResolver {

    StructureAndCommentInfo resolveFromClass(PsiClassReferenceType psiClassReferenceType);

    StructureAndCommentInfo resolveFromParameter(PsiParameter parameter);

    StructureAndCommentInfo resolveFromParameterList(PsiParameterList parameterList);

    StructureAndCommentInfo resolveFromReturnVal(PsiTypeElement returnTypeElement);

}
