package cn.gudqs7.plugins.search;

import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.actions.searcheverywhere.ClassSearchEverywhereContributor;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 */
public class GotoApiAction extends GotoActionBase implements DumbAware {

    public GotoApiAction() {
        //we need to change the template presentation to show the proper text for the action in Settings | Keymap
        Presentation presentation = getTemplatePresentation();
        presentation.setText("Go to Search Everywhere --> Api");
        presentation.setDescription("Quickly navigate to a Api by url");
        addTextOverride(ActionPlaces.MAIN_MENU, "Api");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
          return;
        }

        boolean dumb = DumbService.isDumb(project);
        if (Registry.is("new.search.everywhere")) {
            if (!dumb || new ClassSearchEverywhereContributor(e).isDumbAware()) {
                //noinspection UnstableApiUsage
                showInSearchEverywherePopup(ApiSearchContributor.class.getSimpleName(), e, true, true);
            }
        }
    }

    @Override
    protected void gotoActionPerformed(@NotNull AnActionEvent e) {

    }

}
