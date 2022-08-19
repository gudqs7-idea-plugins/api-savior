package cn.gudqs7.plugins.tools;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import org.jetbrains.annotations.NotNull;

/**
 * @author wenquan
 * @date 2022/8/19
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class GenerateQueryApiDeclarationAction extends AbstractGenerateApiDeclarationAction {

    public GenerateQueryApiDeclarationAction() {
        super("ApiQuery", true);
    }

    @Override
    public @NotNull
    @IntentionFamilyName String getFamilyName() {
        return "Generate Query Api Method";
    }

    @Override
    public @IntentionName
    @NotNull String getText() {
        return "Generate Query Api Method";
    }
}
