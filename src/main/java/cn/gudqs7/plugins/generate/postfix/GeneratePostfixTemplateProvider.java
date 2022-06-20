package cn.gudqs7.plugins.generate.postfix;

import cn.gudqs7.plugins.generate.convert.template.GenerateConvertTemplate;
import cn.gudqs7.plugins.generate.getter.template.GenerateAllGetterTemplate;
import cn.gudqs7.plugins.generate.setter.template.GenerateAllSetterByBuilderTemplate;
import cn.gudqs7.plugins.generate.setter.template.GenerateAllSetterWithChainTemplate;
import cn.gudqs7.plugins.generate.setter.template.GenerateAllSetterWithDefaultValTemplate;
import cn.gudqs7.plugins.generate.setter.template.GenerateAllSetterWithoutDefaultValTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author WQ
 */
public class GeneratePostfixTemplateProvider implements PostfixTemplateProvider {

    private final Set<PostfixTemplate> templates;

    public GeneratePostfixTemplateProvider() {
        templates = ContainerUtil.newHashSet(
                new GenerateAllSetterWithDefaultValTemplate(),
                new GenerateAllGetterTemplate(),
                new GenerateAllSetterWithoutDefaultValTemplate(),
                new GenerateConvertTemplate(),
                new GenerateAllSetterByBuilderTemplate(),
                new GenerateAllSetterWithChainTemplate()
        );
    }

    @NotNull
    @Override
    public Set<PostfixTemplate> getTemplates() {
        return templates;
    }

    @Override
    public boolean isTerminalSymbol(char currentChar) {
        return false;
    }

    @Override
    public void preExpand(@NotNull PsiFile file, @NotNull Editor editor) {

    }

    @Override
    public void afterExpand(@NotNull PsiFile file, @NotNull Editor editor) {

    }

    @Override
    public @NotNull PsiFile preCheck(@NotNull PsiFile copyFile, @NotNull Editor realEditor, int currentOffset) {
        return copyFile;
    }
}