package cn.gudqs7.plugins.savior.action.batch;

import cn.gudqs7.plugins.common.base.action.AbstractBatchDocerSavior;
import cn.gudqs7.plugins.common.enums.PluginSettingEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.util.PluginSettingHelper;
import cn.gudqs7.plugins.common.util.file.FileUtil;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.savior.savior.more.JavaToAmpSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 导出相应信息成 AMP 格式 yaml
 * @author wenquan
 * @date 2022/3/30
 */
public class AmpDocerSaviorAction extends AbstractBatchDocerSavior {

    protected JavaToAmpSavior javaToAmpSavior;

    public AmpDocerSaviorAction() {
        this.javaToAmpSavior = new JavaToAmpSavior(ThemeHelper.getRestfulTheme());
    }

    @Override
    protected boolean isNotShow(@NotNull AnActionEvent e, Project project, PsiElement psiElement, PsiClass psiClass, PsiDirectory psiDirectory) {
        return !PluginSettingHelper.getConfigItem(PluginSettingEnum.AMP_ENABLE, false);
    }

    @Override
    protected String runLoop0(PsiClass psiClass0, Project project, CommentInfo commentInfo, String moduleName, String fileName, String fullFileName, Map<String, Object> otherMap) throws Throwable {
        // 将数据格式化成 yaml
        Map<String, Object> apis = javaToAmpSavior.generateAmpScheme(psiClass0, project);
        if (MapUtils.isEmpty(apis)) {
            return null;
        }
        // 将 apis 收集起来
        Map<String, Object> allApis = (Map<String, Object>) otherMap.get("apis");
        allApis.putAll(apis);

        return generateYaml(apis);
    }

    @Override
    protected void runLoopBefore(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) throws Throwable {
        Map<String, Object> allApis = new LinkedHashMap<>(16);
        otherMap.put("apis", allApis);
    }

    @Override
    protected void runLoopAfter(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) throws Throwable {
        Map<String, Object> allApis = (Map<String, Object>) otherMap.get("apis");
        String yaml = generateYaml(allApis);
        if (MapUtils.isEmpty(allApis)) {
            return;
        }

        String projectName = project.getName();
        File parent = new File(docRootDirPath);
        FileUtil.writeStringToFile(yaml, parent, projectName + "-amp.yaml");
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        return PsiClassUtil.isControllerOrFeign(psiClass);
    }

    @Override
    protected String getDirPrefix() {
        return "amp";
    }

    @Override
    protected @NotNull String getFileExtension() {
        return "yaml";
    }

    private String generateYaml(Map<String, Object> apis) throws JsonProcessingException {
        Map<String, Object> root = new HashMap<>(4);
        root.put("apis", apis);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(root);
        JsonNode jsonNodeTree = objectMapper.readTree(json);
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }
}
