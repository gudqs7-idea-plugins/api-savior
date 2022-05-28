package cn.gudqs7.plugins.search;

import cn.gudqs7.plugins.search.resolver.ApiNavigationItem;
import cn.gudqs7.plugins.search.resolver.ApiResolverService;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wq
 * @date 2022/5/28
 */
public class ApiChooseByNameContributor implements ChooseByNameContributor {

    private final List<ApiNavigationItem> apiNavigationItemList = new ArrayList<>();

    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        apiNavigationItemList.addAll(ApiResolverService.getInstance(project).getApiNavigationItemList());
        return apiNavigationItemList.stream().map(ApiNavigationItem::getName).distinct().toArray(String[]::new);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        return apiNavigationItemList.stream().filter(r -> r.getName().equals(name)).toArray(NavigationItem[]::new);
    }

}
