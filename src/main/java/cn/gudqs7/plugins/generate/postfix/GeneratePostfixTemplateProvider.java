package cn.gudqs7.plugins.generate.postfix;

import cn.gudqs7.plugins.generate.convert.template.GenerateConvertTemplate;
import cn.gudqs7.plugins.generate.getter.template.GenerateAllGetterTemplate;
import cn.gudqs7.plugins.generate.setter.template.GenerateAllSetterByBuilderTemplate;
import cn.gudqs7.plugins.generate.setter.template.GenerateAllSetterWithDefaultValTemplate;
import cn.gudqs7.plugins.generate.setter.template.GenerateAllSetterWithoutDefaultValTemplate;
import com.intellij.codeInsight.template.postfix.templates.JavaPostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author WQ
 */
public class GeneratePostfixTemplateProvider extends JavaPostfixTemplateProvider {

    private final Set<PostfixTemplate> templates;

    public GeneratePostfixTemplateProvider() {
        templates = ContainerUtil.newHashSet(
                new GenerateAllSetterWithDefaultValTemplate(),
                new GenerateAllGetterTemplate(),
                new GenerateAllSetterWithoutDefaultValTemplate(),
                new GenerateConvertTemplate(),
                new GenerateAllSetterByBuilderTemplate()
        );
    }

    @NotNull
    @Override
    public Set<PostfixTemplate> getTemplates() {
        return templates;
    }
}