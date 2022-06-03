package cn.gudqs7.plugins.savior.docer.action.base;

import cn.gudqs7.plugins.savior.docer.pojo.FieldLevelInfo;
import cn.gudqs7.plugins.savior.docer.pojo.PostmanKvInfo;
import cn.gudqs7.plugins.savior.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.savior.docer.reader.Java2ApiReader;
import cn.gudqs7.plugins.savior.docer.reader.Java2BulkReader;
import cn.gudqs7.plugins.savior.docer.reader.Java2JsonReader;
import cn.gudqs7.plugins.savior.docer.resolver.StructureAndCommentResolver;
import cn.gudqs7.plugins.savior.docer.theme.Theme;
import cn.gudqs7.plugins.savior.docer.util.ClipboardUtil;
import cn.gudqs7.plugins.savior.docer.util.FreeMarkerUtil;
import cn.gudqs7.plugins.savior.docer.util.JsonUtil;
import cn.gudqs7.plugins.savior.docer.util.RestfulUtil;
import cn.gudqs7.plugins.savior.generate.util.BaseTypeUtil;
import cn.gudqs7.plugins.savior.util.PsiClassUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 */
public abstract class AbstractReqDocerSavior extends AbstractOnRightClickSavior {

    private final Java2JsonReader java2JsonReader;
    private final Java2ApiReader java2ApiReader;
    private final Java2BulkReader java2BulkReader;
    private final StructureAndCommentResolver structureAndCommentResolver;

    public AbstractReqDocerSavior(Theme theme) {
        this.java2JsonReader = new Java2JsonReader(theme);
        this.java2ApiReader = new Java2ApiReader(theme);
        this.java2BulkReader = new Java2BulkReader(theme);
        structureAndCommentResolver = new StructureAndCommentResolver(theme);
    }

    @Override
    protected void checkPsiMethod(PsiMethod psiMethod, Project project, AnActionEvent e) {
        notVisible(e);
    }

    @Override
    protected void checkPsiClass(PsiClass psiClass, Project project, AnActionEvent e) {
        // 仅普通 Java Bean 显示
        if (psiClass.isInterface()) {
            notVisible(e);
            return;
        }
        if (PsiClassUtil.isControllerOrFeign(psiClass)) {
            notVisible(e);
            return;
        }
        String presentableText = psiClass.getName();
        if (BaseTypeUtil.isBaseTypeOrObject(psiClass)) {
            notVisible(e);
        }
    }

    @Override
    protected void handlePsiClass(Project project, PsiClass psiClass) {
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null) {
            return;
        }
        PsiClassType psiClassType = PsiType.getTypeByName(qualifiedName, project, GlobalSearchScope.allScope(project));
        if (psiClassType instanceof PsiClassReferenceType) {
            PsiClassReferenceType classReferenceType = (PsiClassReferenceType) psiClassType;
            structureAndCommentResolver.setProject(project);
            StructureAndCommentInfo structureAndCommentInfo = structureAndCommentResolver.resolveFromClass(classReferenceType);
            String java2json = "";
            Theme theme = java2ApiReader.getTheme();
            switch (theme.getThemeType()) {
                case RPC:
                    Map<String, Object> map = java2JsonReader.read(structureAndCommentInfo);
                    if (map != null) {
                        java2json = JsonUtil.toJson(map);
                    }
                    break;
                case RESTFUL:
                    List<PostmanKvInfo> kvInfoList = java2BulkReader.read(structureAndCommentInfo);
                    java2json = RestfulUtil.getPostmanBulkByKvList(kvInfoList);
                default:
                    break;
            }
            Map<String, List<FieldLevelInfo>> levelMap = java2ApiReader.read(structureAndCommentInfo);
            Map<String, Object> root = new HashMap<>();
            root.put("levelMap", levelMap);
            String template = FreeMarkerUtil.renderTemplate(theme.getFieldPath(), root);
            ClipboardUtil.setSysClipboardText(java2json);
            String message = "已自动的将示例复制到您的剪切板!\n您可以粘贴后再复制下面的参数说明Markdown";
            Messages.showMultilineInputDialog(project, message, "可以粘贴(Ctrl+V)了", template, Messages.getInformationIcon(), null);
        }
    }

}
