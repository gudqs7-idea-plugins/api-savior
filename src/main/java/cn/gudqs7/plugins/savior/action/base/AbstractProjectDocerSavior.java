package cn.gudqs7.plugins.savior.action.base;

import cn.gudqs7.plugins.common.base.action.AbstractBatchDocerSavior;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.util.StringTool;
import cn.gudqs7.plugins.common.util.file.FileUtil;
import cn.gudqs7.plugins.common.util.jetbrain.ClipboardUtil;
import cn.gudqs7.plugins.common.util.jetbrain.DialogUtil;
import cn.gudqs7.plugins.savior.savior.more.JavaToDocSavior;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wq
 */
public abstract class AbstractProjectDocerSavior extends AbstractBatchDocerSavior {

    public static final String TOC = " \\[返回目录]\\(#目录\\)";
    protected JavaToDocSavior docSavior;

    public AbstractProjectDocerSavior(JavaToDocSavior docSavior) {
        this.docSavior = docSavior;
    }

    @Override
    protected String runLoop0(PsiClass psiClass0, Project project, CommentInfo commentInfo, String moduleName, String fileName, String fullFileName, Map<String, Object> otherMap) throws Throwable {
        Pair<String, List<String>> classMarkdownPair = docSavior.generateApiByServiceInterfaceV2(psiClass0, project);
        String classMarkdown = classMarkdownPair.getLeft();
        if (StringUtils.isBlank(classMarkdown)) {
            return "";
        }
        // 检查该类下方法名是否重复
        List<String> apiNameList = classMarkdownPair.getRight();
        String classMarkdown0 = classMarkdown;
        List<String> apiNameList0 = new ArrayList<>(32);
        Set<String> methodSet = (Set<String>) otherMap.get("methodSet");
        Map<String, Integer> apiNameNoMap = (Map<String, Integer>) otherMap.get("apiNameNoMap");
        for (String apiName : apiNameList) {
            String apiNameEscape = StringTool.escapeRegex(apiName);
            String originH1 = "# " + apiNameEscape;
            // 先为每个接口添加返回目录跳转链接
            if (methodSet.contains(apiName)) {
                // 修改方法名
                String apiName0 = apiName + generateNo(apiNameNoMap, apiName);
                String tempH1 = "__" + apiNameEscape + "__";

                // 先将第一个不带数字的替换掉
                classMarkdown0 = classMarkdown0.replaceFirst(originH1, tempH1);
                // 替换第一个非数字结尾的
                classMarkdown0 = classMarkdown0.replaceFirst(originH1 + "(?!\\d+)", "# " + apiName0);
                // 恢复第一个不带数字的
                classMarkdown0 = classMarkdown0.replaceFirst(tempH1, originH1);
                apiNameList0.add(apiName0);
                continue;
            }
            classMarkdown0 = classMarkdown0.replaceAll(originH1, TOC + "\n" + originH1);
            apiNameList0.add(apiName);
            methodSet.add(apiName);
        }


        // 拼接 markdown 内容
        StringBuilder allMarkdownSbf = (StringBuilder) otherMap.get("allMarkdown");
        allMarkdownSbf.append(classMarkdown0).append("\n");

        // 收集目录信息
        Map<String, Map<String, List<String>>> toc = (Map<String, Map<String, List<String>>>) otherMap.get("toc");
        Map<String, List<String>> moduleMarkdownMap = toc.computeIfAbsent(moduleName, ignore -> new LinkedHashMap<>(16));
        moduleMarkdownMap.put(fileName, apiNameList0);
        return classMarkdown;
    }

    @Override
    protected void runLoopBefore(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) throws Throwable {
        // 初始化
        StringBuilder allMarkdownSbf = new StringBuilder();
        Map<String, Map<String, List<String>>> toc = new LinkedHashMap<>(16);
        Set<String> methodSet = new HashSet<>(32);
        Map<String, Integer> apiNameNoMap = new HashMap<>(32);
        otherMap.put("allMarkdown", allMarkdownSbf);
        otherMap.put("toc", toc);
        otherMap.put("methodSet", methodSet);
        otherMap.put("apiNameNoMap", apiNameNoMap);
    }

    @Override
    protected void runLoopAfter(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) throws Throwable {
        StringBuilder allMarkdownSbf = (StringBuilder) otherMap.get("allMarkdown");
        if (allMarkdownSbf == null || allMarkdownSbf.length() == 0) {
            return;
        }

        StringBuilder tocMarkdown = new StringBuilder();
        tocMarkdown.append("# 目录").append("\n");

        Map<String, Map<String, List<String>>> toc = (Map<String, Map<String, List<String>>>) otherMap.get("toc");
        for (Map.Entry<String, Map<String, List<String>>> entry : toc.entrySet()) {
            String moduleName = entry.getKey();
            Map<String, List<String>> moduleMap = entry.getValue();

            tocMarkdown.append("- ").append(moduleName).append("\n");
            for (Map.Entry<String, List<String>> moduleEntry : moduleMap.entrySet()) {
                String fileName = moduleEntry.getKey();
                tocMarkdown.append("  - ").append(fileName).append("\n");

                List<String> apiNameList = moduleEntry.getValue();
                for (String apiName : apiNameList) {
                    String apiNameRemove = StringTool.removeRegex(apiName);
                    tocMarkdown.append("    - ").append(String.format("[%s](#%s)", apiName, apiNameRemove)).append("\n");
                }
            }
        }

        String fullMarkdown = tocMarkdown + "\n" + allMarkdownSbf;
        String projectName = project.getName();
        File parent = new File(docRootDirPath);
        // todo 考虑添加配置, 是否对文件名加时间戳和Ymd信息
        FileUtil.writeStringToFile(fullMarkdown, parent, projectName + "-" + getDirPrefix() + "-all.md");
    }

    @Override
    protected boolean handleRightClickOnClass(Project project, PsiClass psiClass, Set<PsiClass> psiClassList) {
        if (isNeedDealPsiClass(psiClass, project)) {
            String docByInterface = docSavior.generateApiByServiceInterface(psiClass, project);
            ClipboardUtil.setSysClipboardText(docByInterface);
            DialogUtil.showDialog(project, "已自动的将 Markdown 文档复制到您的剪切板!\n您可在此预览后再去粘贴!", docByInterface);
            return true;
        }
        return false;
    }

    private Integer generateNo(Map<String, Integer> apiNameNoMap, String apiName) {
        Integer no = apiNameNoMap.get(apiName);
        if (no == null) {
            no = 2;
        }
        apiNameNoMap.put(apiName, no + 1);
        return no;
    }

}
