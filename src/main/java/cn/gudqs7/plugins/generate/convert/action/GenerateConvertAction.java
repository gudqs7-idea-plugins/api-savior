package cn.gudqs7.plugins.generate.convert.action;

import cn.gudqs7.plugins.common.util.StringTool;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.common.util.structure.PsiMethodUtil;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.base.GenerateBase;
import cn.gudqs7.plugins.generate.base.GenerateBaseAction;
import cn.gudqs7.plugins.generate.consant.GenerateConst;
import cn.gudqs7.plugins.generate.convert.GenerateConvertForMethod;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author WQ
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class GenerateConvertAction extends GenerateBaseAction {

    @Override
    protected boolean checkVariableClass(PsiClass psiClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected GenerateBase buildGenerateByVar(BaseVar baseVar) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isAvailable0(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiJavaToken) {
            PsiJavaToken psiJavaToken = (PsiJavaToken) psiElement;
            IElementType tokenType = psiJavaToken.getTokenType();
            String tokenTypeName = tokenType.toString();
            if ("LPARENTH".equals(tokenTypeName)) {
                PsiElement parent = psiElement.getParent();
                if (parent != null) {
                    if (parent.getParent() instanceof PsiMethod) {
                        PsiMethod psiMethod = (PsiMethod) parent.getParent();
                        return isAvailableByPsiMethod(psiMethod);
                    }
                }
            }
        }
        if (psiElement instanceof PsiIdentifier) {
            PsiElement parent = psiElement.getParent();
            if (parent instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) parent;
                return isAvailableByPsiMethod(psiMethod);
            }
        }
        return false;
    }

    @Override
    protected GenerateBase buildGenerate(PsiElement psiElement) {
        if (psiElement instanceof PsiJavaToken) {
            PsiJavaToken psiJavaToken = (PsiJavaToken) psiElement;
            IElementType tokenType = psiJavaToken.getTokenType();
            String tokenTypeName = tokenType.toString();
            if ("LPARENTH".equals(tokenTypeName)) {
                PsiElement parent = psiElement.getParent();
                if (parent != null) {
                    if (parent.getParent() instanceof PsiMethod) {
                        PsiMethod psiMethod = (PsiMethod) parent.getParent();
                        return buildGenerateByPsiMethod(psiMethod);
                    }
                }
            }
        }
        if (psiElement instanceof PsiIdentifier) {
            PsiElement parent = psiElement.getParent();
            if (parent instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) parent;
                return buildGenerateByPsiMethod(psiMethod);
            }
        }
        return null;
    }

    @Override
    protected void invoke0(Project project, Editor editor, PsiElement psiElement, Document document, PsiDocumentManager psiDocumentManager) {
        GenerateBase generateBase = buildGenerate(psiElement);
        PsiFile containingFile = psiElement.getContainingFile();
        if (psiElement instanceof PsiJavaToken) {
            PsiJavaToken psiJavaToken = (PsiJavaToken) psiElement;
            IElementType tokenType = psiJavaToken.getTokenType();
            String tokenTypeName = tokenType.toString();
            if ("LPARENTH".equals(tokenTypeName)) {
                PsiElement parent = psiElement.getParent();
                if (parent != null) {
                    if (parent.getParent() instanceof PsiMethod) {
                        PsiMethod psiMethod = (PsiMethod) parent.getParent();
                        invokeByPsiMethod(generateBase, psiDocumentManager, containingFile, document, psiMethod);
                    }
                }
            }
        }
        if (psiElement instanceof PsiIdentifier) {
            PsiElement parent = psiElement.getParent();
            if (parent instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) parent;
                invokeByPsiMethod(generateBase, psiDocumentManager, containingFile, document, psiMethod);
            }
        }
    }


    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return GenerateConst.GENERATE_CONVERT;
    }

    @NotNull
    @Override
    public String getText() {
        return GenerateConst.GENERATE_CONVERT;
    }

    private boolean isAvailableByPsiMethod(PsiMethod psiMethod) {
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        PsiType returnType = psiMethod.getReturnType();
        if (returnType != null && parameters.length == 1) {
            boolean srcClassHasValidGetter = false;
            boolean dstClassHasValidSetter = false;
            PsiParameter psiParameter = parameters[0];
            PsiType psiType = psiParameter.getType();
            PsiClass srcPsiClass = PsiClassUtil.getPsiClassByPsiType(psiType);
            if (srcPsiClass != null) {
                srcClassHasValidGetter = PsiMethodUtil.checkClassHasValidGetter(srcPsiClass);
            }
            PsiClass dstPsiClass = PsiClassUtil.getPsiClassByPsiType(returnType);
            if (dstPsiClass != null) {
                dstClassHasValidSetter = PsiMethodUtil.checkClassHasValidSetter(dstPsiClass);
            }
            // 当源对象有 Get 方法, 目标对象有 Set 方法, 即认为可以进行 Convert
            return srcClassHasValidGetter && dstClassHasValidSetter;
        } else {
            return false;
        }
    }

    @Nullable
    private GenerateConvertForMethod buildGenerateByPsiMethod(PsiMethod psiMethod) {
        PsiType dstPsiType = psiMethod.getReturnType();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        if (dstPsiType != null && parameters.length == 1) {
            PsiParameter psiParameter = parameters[0];
            PsiType srcPsiType = psiParameter.getType();
            String dstTypeName = dstPsiType.getPresentableText();
            BaseVar varForSet = new BaseVar();
            varForSet.setVarName(StringTool.toCamelCase(dstTypeName));
            varForSet.setVarType(dstPsiType);

            BaseVar varForGet = new BaseVar();
            varForGet.setVarName(psiParameter.getName());
            varForGet.setVarType(srcPsiType);
            return new GenerateConvertForMethod(varForSet, varForGet);
        } else {
            return null;
        }
    }

    private void invokeByPsiMethod(GenerateBase generateBase, PsiDocumentManager psiDocumentManager, PsiFile containingFile, Document document, PsiMethod psiMethod) {
        String prefix = getPrefixWithBreakLine(document, psiMethod);
        Integer insertOffset = getInsertOffset(psiMethod);
        generateBase.insertCodeByPsiType(document, psiDocumentManager, containingFile, prefix, insertOffset);
    }

}
