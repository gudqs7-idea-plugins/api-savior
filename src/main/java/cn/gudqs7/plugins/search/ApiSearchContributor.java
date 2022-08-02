package cn.gudqs7.plugins.search;

import cn.gudqs7.plugins.common.enums.HttpMethod;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.search.icon.IconHolder;
import cn.gudqs7.plugins.search.resolver.ApiNavigationItem;
import cn.gudqs7.plugins.search.resolver.ApiResolverService;
import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ide.actions.bigPopup.ShowFilterAction;
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;
import com.intellij.ide.actions.searcheverywhere.PersistentSearchEverywhereContributorFilter;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor;
import com.intellij.ide.util.ElementsChooser;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.Processor;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author wq
 * @date 2022/5/28
 */
public class ApiSearchContributor implements WeightedSearchEverywhereContributor<ApiNavigationItem> {

    private final AnActionEvent actionEvent;
    private final Project myProject;
    private PersistentSearchEverywhereContributorFilter<HttpMethod> myFilter;
    private List<ApiNavigationItem> navItemList;

    public ApiSearchContributor(@NotNull AnActionEvent event) {
        this.actionEvent = event;
        myProject = event.getRequiredData(CommonDataKeys.PROJECT);
        MethodFilterConfiguration methodFilterConfiguration = MethodFilterConfiguration.getInstance(myProject);
        if (methodFilterConfiguration != null) {
            myFilter = new PersistentSearchEverywhereContributorFilter<>(
                    Arrays.asList(HttpMethod.values()), methodFilterConfiguration,
                    Enum::name, httpMethod -> null
            );
        }
    }

    @NotNull
    @Override
    public String getSearchProviderId() {
        return getClass().getSimpleName();
    }

    @NotNull
    @Override
    public String getGroupName() {
        return "Api";
    }

    @Override
    public int getSortWeight() {
        return 800;
    }

    @Nullable
    @Override
    public String getAdvertisement() {
        return DumbService.isDumb(myProject) ? "Results might be incomplete. The project is being indexed." : "type url or to search";
    }

    @Override
    public boolean processSelectedItem(@NotNull ApiNavigationItem selected, int modifiers, @NotNull String searchText) {
        PsiNavigateUtil.navigate(selected.getPsiElement());
        return true;
    }

    @NotNull
    @Override
    public ListCellRenderer<Object> getElementsRenderer() {
        return new SearchEverywherePsiRenderer(this) {

            @Override
            protected boolean customizeNonPsiElementLeftRenderer(ColoredListCellRenderer renderer, JList list, Object value, int index, boolean selected, boolean hasFocus) {
                try {
                    Color fgColor = list.getForeground();
                    Color bgColor = UIUtil.getListBackground();
                    SimpleTextAttributes nameAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, fgColor);

                    ItemMatchers itemMatchers = getItemMatchers(list, value);
                    ApiNavigationItem apiNavigationItem = (ApiNavigationItem) value;
                    String name = apiNavigationItem.getUrl();
                    String locationString = " " + apiNavigationItem.getRightText();

                    SpeedSearchUtil.appendColoredFragmentForMatcher(name, renderer, nameAttributes, itemMatchers.nameMatcher, bgColor, selected);
                    renderer.setIcon(IconHolder.getHttpMethodIcon(apiNavigationItem.getHttpMethod()));

                    if (StringUtils.isNotEmpty(locationString)) {
                        FontMetrics fm = list.getFontMetrics(list.getFont());
                        int maxWidth = list.getWidth() - fm.stringWidth(name) - myRightComponentWidth - 36;
                        int fullWidth = fm.stringWidth(locationString);
                        if (fullWidth < maxWidth) {
                            SpeedSearchUtil.appendColoredFragmentForMatcher(locationString, renderer, SimpleTextAttributes.GRAYED_ATTRIBUTES, itemMatchers.nameMatcher, bgColor, selected);
                        } else {
                            int adjustedWidth = Math.max(locationString.length() * maxWidth / fullWidth - 1, 3);
                            locationString = StringUtil.trimMiddle(locationString, adjustedWidth);
                            SpeedSearchUtil.appendColoredFragmentForMatcher(locationString, renderer, SimpleTextAttributes.GRAYED_ATTRIBUTES, itemMatchers.nameMatcher, bgColor, selected);
                        }
                    }
                    return true;
                } catch (Throwable ex) {
                    ExceptionUtil.handleException(ex);
                    return false;
                }
            }
        };
    }

    @Nullable
    @Override
    public Object getDataForItem(@NotNull ApiNavigationItem element, @NotNull String dataId) {
        return null;
    }

    @Override
    public boolean isEmptyPatternSupported() {
        return true;
    }

    @Override
    public boolean isShownInSeparateTab() {
        return true;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public boolean isDumbAware() {
        return DumbService.isDumb(myProject);
    }

    @Override
    public void fetchWeightedElements(@NotNull String pattern, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<? super FoundItemDescriptor<ApiNavigationItem>> consumer) {
        try {
            if (isDumbAware() || !shouldProvideElements(pattern)) {
                return;
            }

            MinusculeMatcher matcher = NameUtil.buildMatcher("*" + pattern + "*", NameUtil.MatchingCaseSensitivity.NONE);
            Set<HttpMethod> httpMethodSet = new HashSet<>();
            if (myFilter == null) {
                httpMethodSet.addAll(Arrays.asList(HttpMethod.values()));
            } else {
                httpMethodSet.addAll(myFilter.getSelectedElements());
            }
            boolean selectAll = httpMethodSet.size() == HttpMethod.values().length;

            // 从ALL -> URL Tab或快捷键进入时列表为空
            if (navItemList == null) {
                // 必须从read线程访问，耗时不能过长
                navItemList = ApplicationManager.getApplication().runReadAction(
                        (ThrowableComputable<List<ApiNavigationItem>, Throwable>) () ->
                                ApiResolverService.getInstance(myProject).getApiNavigationItemList()
                );
            }
            if (navItemList != null) {
                for (ApiNavigationItem restItem : navItemList) {
                    if (selectAll || httpMethodSet.contains(restItem.getHttpMethod())) {
                        if (matcher.matches(restItem.getUrl()) || matcher.matches(restItem.getMethodPathInfo().getMethodDesc())) {
                            if (!consumer.process(new FoundItemDescriptor<>(restItem, 0))) {
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            ExceptionUtil.handleException(ex);
        }
    }

    @Override
    public @NotNull List<AnAction> getActions(@NotNull Runnable onChanged) {
        // 获取 Filter 的 action
        if (myProject == null || myFilter == null) {
            return Collections.emptyList();
        }
        ArrayList<AnAction> result = new ArrayList<>();
        result.add(new FiltersAction(myFilter, onChanged));
        return result;
    }

    /**
     * 判断是否应该返回列表元素
     *
     * @param pattern 搜索词
     */
    private boolean shouldProvideElements(String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            return true;
        }
        SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(myProject);
        if (seManager.isShown()) {
            // 非 All Tab
            return getSearchProviderId().equals(seManager.getSelectedTabID());
        } else {
            // ALL Tab
            return !ActionsBundle.message("action.SearchEverywhere.text").equals(actionEvent.getPresentation().getText());
        }
    }

    static class FiltersAction extends ShowFilterAction {

        final PersistentSearchEverywhereContributorFilter<?> filter;
        final Runnable rebuildRunnable;

        FiltersAction(@NotNull PersistentSearchEverywhereContributorFilter<?> filter, @NotNull Runnable rebuildRunnable) {
            this.filter = filter;
            this.rebuildRunnable = rebuildRunnable;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        protected boolean isActive() {
            return filter.getAllElements().size() != filter.getSelectedElements().size();
        }

        @Override
        protected ElementsChooser<?> createChooser() {
            return createChooser(filter, rebuildRunnable);
        }

        private static <T> ElementsChooser<T> createChooser(@NotNull PersistentSearchEverywhereContributorFilter<T> filter, @NotNull Runnable rebuildRunnable) {
            ElementsChooser<T> res = new ElementsChooser<T>(filter.getAllElements(), false) {
                @Override
                protected String getItemText(@NotNull T value) {
                    return filter.getElementText(value);
                }

                @Nullable
                @Override
                protected Icon getItemIcon(@NotNull T value) {
                    return filter.getElementIcon(value);
                }
            };
            res.markElements(filter.getSelectedElements());
            ElementsChooser.ElementsMarkListener<T> listener = (element, isMarked) -> {
                filter.setSelected(element, isMarked);
                rebuildRunnable.run();
            };
            res.addElementsMarkListener(listener);
            return res;
        }

    }
}
