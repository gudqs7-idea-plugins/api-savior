package com.zhaow.restful.navigation.action;


import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author restful dalao
 */
public class GotoRequestMappingProvider extends DefaultChooseByNameItemProvider {

    public GotoRequestMappingProvider(@Nullable PsiElement context) {
        super(context);
    }

}
