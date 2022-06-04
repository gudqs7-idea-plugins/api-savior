package cn.gudqs7.plugins.savior.action.batch;

import cn.gudqs7.plugins.common.base.action.AbstractBatchDocerSavior;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.util.FileUtil;
import cn.gudqs7.plugins.common.util.FreeMarkerUtil;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.savior.savior.more.JavaToDocSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.youbenzi.mdtool.tool.MDTool;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 导出相应信息成 AMP 格式 yaml
 *
 * @author wenquan
 * @date 2022/3/30
 */
public class HtmlDocerSaviorAction extends AbstractBatchDocerSavior {

    protected JavaToDocSavior docSavior;

    public HtmlDocerSaviorAction() {
        this.docSavior = new JavaToDocSavior(ThemeHelper.getRestfulTheme());
    }

    @Override
    protected String runLoop0(PsiClass psiClass0, Project project, CommentInfo commentInfo, String moduleName, String fileName, String fullFileName, Map<String, Object> otherMap) {
        Pair<String, List<String>> markdownPair = docSavior.generateApiByServiceInterfaceV2(psiClass0, project);
        String markdown = markdownPair.getLeft();
        List<String> apiNameList = markdownPair.getRight();
        if (StringUtils.isBlank(markdown)) {
            return "";
        }

        List<CategoryItem> categoryItemList = (List<CategoryItem>) otherMap.computeIfAbsent("categoryItemList", k -> new ArrayList<>());
        for (String apiName : apiNameList) {
            categoryItemList.add(new CategoryItem(moduleName, fileName, moduleName + "/" + fullFileName + "#" + apiName, apiName));
        }
        String markdown2Html = MDTool.markdown2Html(markdown);
        Map<String, Object> root = new HashMap<>(8);
        root.put("title", moduleName + "-" + fileName);
        root.put("markdownDoc", markdown2Html);
        return FreeMarkerUtil.renderTemplate("html-doc.ftl", root);
    }

    @Override
    protected void runLoopAfter(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) {
        List<CategoryItem> categoryItemList = (List<CategoryItem>) otherMap.computeIfAbsent("categoryItemList", k -> new ArrayList<>());
        if (CollectionUtils.isNotEmpty(categoryItemList)) {
            List<Module> moduleList = new ArrayList<>();
            Map<String, List<CategoryItem>> moduleMap = categoryItemList.stream().collect(Collectors.groupingBy(CategoryItem::getModuleName));
            for (Map.Entry<String, List<CategoryItem>> entry : moduleMap.entrySet()) {
                String key = entry.getKey();
                List<CategoryItem> value = entry.getValue();
                List<FileDir> fileDirList = new ArrayList<>();
                Map<String, List<CategoryItem>> fileDirMap = value.stream().collect(Collectors.groupingBy(CategoryItem::getFileName));
                for (Map.Entry<String, List<CategoryItem>> listEntry : fileDirMap.entrySet()) {
                    String dirKey = listEntry.getKey();
                    List<CategoryItem> dirValue = listEntry.getValue();

                    FileDir fileDir = new FileDir();
                    fileDir.setFileName(dirKey);
                    fileDir.setCategoryItemList(dirValue);
                    fileDirList.add(fileDir);
                }

                Module module = new Module();
                module.setModuleName(key);
                module.setFileDirList(fileDirList);

                moduleList.add(module);
            }
            Map<String, Object> root = new HashMap<>(8);
            root.put("moduleList", moduleList);
            String template = FreeMarkerUtil.renderTemplate("index.ftl", root);
            File parent = new File(docRootDirPath);
            FileUtil.writeStringToFile(template, parent, "index.html");
        }
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        return PsiClassUtil.isControllerOrFeign(psiClass);
    }

    @Override
    protected String getDirPrefix() {
        return "html";
    }

    @Override
    protected @NotNull String getFileExtension() {
        return "html";
    }

    @Data
    public static class CategoryItem {

        private String moduleName;
        private String fileName;
        private String fullFileName;
        private String apiName;

        public CategoryItem(String moduleName, String fileName, String fullFileName, String apiName) {
            this.moduleName = moduleName;
            this.fileName = fileName;
            this.fullFileName = fullFileName;
            this.apiName = apiName;
        }
    }

    @Data
    public static class Module {

        private String moduleName;

        private List<FileDir> fileDirList;

    }

    @Data
    public static class FileDir {

        private String fileName;

        private List<CategoryItem> categoryItemList;

    }

}
