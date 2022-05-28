package cn.gudqs7.plugins.search;

import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wq
 * @date 2022/5/28
 */
public class ApiSearchContributor extends AbstractGotoSEContributor {

    private final ApiGotoByModel apiGotoByModel;

    public ApiSearchContributor(@NotNull AnActionEvent initEvent) {
        super(initEvent);
        this.apiGotoByModel = new ApiGotoByModel(initEvent.getProject(), new ChooseByNameContributor[]{
                new ApiChooseByNameContributor()
        });
    }

    @Override
    protected @NotNull FilteringGotoByModel<?> createModel(@NotNull Project project) {
        return apiGotoByModel;
    }

    @Override
    public @Nullable String getAdvertisement() {
        return DumbService.isDumb(myProject) ? "Results might be incomplete. The project is being indexed." : "type url or to search";
    }

    @Override
    public @NotNull @Nls String getGroupName() {
        return "Api Search";
    }

    @Override
    public int getSortWeight() {
        return 1000;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public boolean isEmptyPatternSupported() {
        return true;
    }
}
