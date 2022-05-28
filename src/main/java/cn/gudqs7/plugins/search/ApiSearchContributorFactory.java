package cn.gudqs7.plugins.search;

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 * @date 2022/5/28
 */
public class ApiSearchContributorFactory implements SearchEverywhereContributorFactory<Object> {

    @Override
    public @NotNull SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent initEvent) {
        return new ApiSearchContributor(initEvent);
    }

}
