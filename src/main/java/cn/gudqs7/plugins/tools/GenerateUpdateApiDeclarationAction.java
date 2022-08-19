package cn.gudqs7.plugins.tools;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import org.jetbrains.annotations.NotNull;

/**
 * @author wenquan
 * @date 2022/8/19
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class GenerateUpdateApiDeclarationAction extends AbstractGenerateApiDeclarationAction {

    public GenerateUpdateApiDeclarationAction() {
        super("ApiUpdate");
    }

    @Override
    public @NotNull
    @IntentionFamilyName String getFamilyName() {
        return "Generate Update Api Method";
    }

    @Override
    public @IntentionName
    @NotNull String getText() {
        return "Generate Update Api Method";
    }

}
