package cn.gudqs7.plugins.search;

import com.intellij.ide.util.gotoByName.CustomMatcherModel;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.zhaow.restful.common.spring.AntPathMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wq
 * @date 2022/5/28
 */
public class ApiGotoByModel extends FilteringGotoByModel<FileType> implements DumbAware, CustomMatcherModel {

    protected ApiGotoByModel(@NotNull Project project, ChooseByNameContributor[] contributors) {
        super(project, contributors);
    }

    @Override
    protected @Nullable FileType filterValueFor(NavigationItem item) {
        return null;
    }

    @Override
    public String getPromptText() {
        return "Enter Api url";
    }

    @Override
    public @NotNull String getNotInMessage() {
        return "No matches found";
    }

    @Override
    public @NotNull String getNotFoundMessage() {
        return "Api not found";
    }

    @Override
    public @Nullable String getCheckBoxName() {
        return null;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {

    }

    @NotNull
    @Override
    public String[] getSeparators() {
        return new String[]{"/", "?"};
    }



    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        return getElementName(element);
    }

    @Override
    public boolean willOpenEditor() {
        return true;
    }

    @Override
    public boolean matches(@NotNull String popupItem, @NotNull String userPattern) {
        if("/".equals(userPattern)) {
            return true;
        }
        MinusculeMatcher matcher = NameUtil.buildMatcher("*" + userPattern, NameUtil.MatchingCaseSensitivity.NONE);
        boolean matches = matcher.matches(popupItem);
        if (!matches) {
            AntPathMatcher pathMatcher = new AntPathMatcher();
            matches = pathMatcher.match(popupItem,userPattern);
        }
        return matches;
    }
}
