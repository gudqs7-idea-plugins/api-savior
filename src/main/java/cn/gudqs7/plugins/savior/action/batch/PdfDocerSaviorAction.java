package cn.gudqs7.plugins.savior.action.batch;

import cn.gudqs7.plugins.common.base.action.AbstractBatchDocerSavior;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.util.file.FileUtil;
import cn.gudqs7.plugins.common.util.file.FreeMarkerUtil;
import cn.gudqs7.plugins.common.util.file.MarkdownUtil;
import cn.gudqs7.plugins.common.util.file.PdfUtil;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.savior.savior.more.JavaToDocSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 导出相应信息成 AMP 格式 yaml
 *
 * @author wenquan
 * @date 2022/3/30
 */
public class PdfDocerSaviorAction extends AbstractBatchDocerSavior {

    protected JavaToDocSavior docSavior;

    public PdfDocerSaviorAction() {
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

        String uniqueName = moduleName + "-" + fileName;
        List<CategoryItem> categoryItemList = (List<CategoryItem>) otherMap.computeIfAbsent("categoryItemList", k -> new ArrayList<>());
        List<Markdown> markdownList = (List<Markdown>) otherMap.computeIfAbsent("markdownList", k -> new ArrayList<>());
        for (String apiName : apiNameList) {
            categoryItemList.add(new CategoryItem(moduleName, fileName, uniqueName + "-" + apiName, apiName));
        }
        String markdown2Html = MarkdownUtil.markdownToHtml(markdown, uniqueName + "-");
        Markdown markdown0 = new Markdown();
        markdown0.setUniqueName(uniqueName);
        markdown0.setMarkdownHtml(markdown2Html);
        markdownList.add(markdown0);

        // 此处返回空则不写入文件
        return null;
    }

    @Override
    protected void runLoopAfter(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) {
        List<Markdown> markdownList = (List<Markdown>) otherMap.computeIfAbsent("markdownList", k -> new ArrayList<>());
        Map<String, Markdown> markdownMap = markdownList.stream().collect(Collectors.toMap(
                Markdown::getUniqueName,
                Function.identity(),
                (r1, r2) -> r1
        ));
        List<CategoryItem> categoryItemList = (List<CategoryItem>) otherMap.computeIfAbsent("categoryItemList", k -> new ArrayList<>());
        if (CollectionUtils.isNotEmpty(categoryItemList)) {
            List<Module> moduleList = new ArrayList<>();
            Map<String, List<CategoryItem>> moduleMap = categoryItemList.stream().collect(Collectors.groupingBy(CategoryItem::getModuleName));
            for (Map.Entry<String, List<CategoryItem>> entry : moduleMap.entrySet()) {
                String moduleName = entry.getKey();
                List<CategoryItem> value = entry.getValue();
                List<FileDir> fileDirList = new ArrayList<>();
                Map<String, List<CategoryItem>> fileDirMap = value.stream().collect(Collectors.groupingBy(CategoryItem::getFileName));
                for (Map.Entry<String, List<CategoryItem>> listEntry : fileDirMap.entrySet()) {
                    String fileName = listEntry.getKey();
                    List<CategoryItem> dirValue = listEntry.getValue();

                    FileDir fileDir = new FileDir();
                    fileDir.setFileName(fileName);
                    fileDir.setCategoryItemList(dirValue);

                    String uniqueName = moduleName + "-" + fileName;
                    Markdown markdown = markdownMap.get(uniqueName);
                    if (markdown != null) {
                        fileDir.setMarkdownHtml(markdown.getMarkdownHtml());
                    }

                    fileDirList.add(fileDir);
                }

                Module module = new Module();
                module.setModuleName(moduleName);
                module.setFileDirList(fileDirList);
                moduleList.add(module);
            }
            Map<String, Object> root = new HashMap<>(8);
            root.put("moduleList", moduleList);
            String html = FreeMarkerUtil.renderTemplate("pdf/index.ftl", root);
            File parent = new File(docRootDirPath);
            String projectName = project.getName();
            FileUtil.writeStringToFile(html, parent, "index.html");
            PdfUtil.html2Pdf(html, parent, projectName + "-接口文档.pdf");
        }
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        return PsiClassUtil.isControllerOrFeign(psiClass);
    }

    @Override
    protected String getDirPrefix() {
        return "pdf";
    }

    @Override
    protected @NotNull String getFileExtension() {
        return "pdf";
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

        private String markdownHtml;

    }

    @Data
    public static class Markdown {

        private String uniqueName;

        private String markdownHtml;

    }

}
