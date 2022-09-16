package cn.gudqs7.plugins.generate.setter.action;

import cn.gudqs7.plugins.generate.consant.GenerateConst;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author wenquan
 * @date 2021/9/30
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class SetterWithDefaultValGenerateAction extends AbstractSetterGenerateAction {

    public SetterWithDefaultValGenerateAction() {
        super(true);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return GenerateConst.GENERATE_SETTER;
    }

    @NotNull
    @Override
    public String getText() {
        return GenerateConst.GENERATE_SETTER;
    }

}
