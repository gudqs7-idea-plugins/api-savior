package cn.gudqs7.plugins.docer.action.base;

import cn.gudqs7.plugins.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.util.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wq
 */
public abstract class AbstractBatchDocerSavior extends AnAction {

    public final String API_DOC_ROOT_DIR_NAME = "api-doc";

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            e.getPresentation().setVisible(false);
            return;
        }
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        PsiClass psiClass = ActionUtil.getPsiClass(psiElement);
        PsiDirectory psiDirectory = ActionUtil.getPsiDirectory(psiElement);

        try {
            PsiClass firstClass = getFirstPsiClass(e, project, psiClass, psiDirectory);
            if (firstClass != null) {
                init(e, project, psiElement, firstClass);
            }
            boolean update0 = update0(e, project, psiElement, psiClass, psiDirectory);
            if (update0) {
                e.getPresentation().setVisible(false);
                return;
            }
        } finally {
            destroy(e, project, psiElement);
        }

        boolean isRightClickOnClass = psiClass != null;
        boolean isRightClickOnDirectory = psiDirectory != null;
        if (isRightClickOnClass) {
            if (isNeedDealPsiClass(psiClass, project)) {
                return;
            }
        }
        if (isRightClickOnDirectory) {
            return;
        }

        Object data = e.getDataContext().getData("psi.Element.array");
        if (data instanceof PsiElement[]) {
            PsiElement[] psiElements = (PsiElement[]) data;
            if (psiElements.length > 1) {
                return;
            }
        }

        e.getPresentation().setVisible(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        try {
            PsiClass psiClass = ActionUtil.getPsiClass(psiElement);
            PsiDirectory psiDirectory = ActionUtil.getPsiDirectory(psiElement);

            Set<PsiClass> psiClassList = new TreeSet<>(
                    Comparator.comparing(PsiClass::getQualifiedName)
            );

            boolean isRightClickOnClass = psiClass != null;
            if (isRightClickOnClass) {
                if (handleRightClickOnClass(project, psiClass, psiClassList)) {
                    return;
                }
            }
            psiClassList = getPsiClassList(e, project, psiDirectory, psiClassList);
            if (!CollectionUtils.isEmpty(psiClassList)) {
                PsiClass firstClass = new ArrayList<>(psiClassList).get(0);
                init(e, project, psiElement, firstClass);
                final Set<PsiClass> finalPsiClassList = psiClassList;
                String projectFilePath = project.getBasePath();
                String docRootDirPath = projectFilePath + "/" + API_DOC_ROOT_DIR_NAME;
                String title = getModelTitle();
                AtomicBoolean hasCancelAtomic = new AtomicBoolean(false);
                ProgressManager.getInstance().run(new Task.Modal(project, title, true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        try {
                            indicator.setIndeterminate(false);
                            indicator.setText(getProcessorModelTitle());
                            indicator.setText2(getProcessorModelSubTitle());
                            indicator.setFraction(0.05f);

                            boolean runLoop = isRunLoop();
                            if (runLoop) {
                                // region loop
                                float i = 1f;
                                int size = finalPsiClassList.size();
                                Map<String, Object> otherMap = new HashMap<>(8);
                                runLoopBefore(project, indicator, hasCancelAtomic, finalPsiClassList, docRootDirPath, otherMap);
                                for (PsiClass psiClass0 : finalPsiClassList) {
                                    Thread.sleep(100);
                                    if (indicator.isCanceled()) {
                                        handleCancelTask(docRootDirPath, projectFilePath);
                                        hasCancelAtomic.set(true);
                                        return;
                                    }
                                    AtomicReference<CommentInfo> apiModelPropertyAtomic = new AtomicReference<>(null);
                                    AtomicReference<String> packageNameUniqueAtomic = new AtomicReference<>("");
                                    ApplicationManager.getApplication().invokeAndWait(() -> {
                                        AnnotationHolder psiClassHolder = AnnotationHolder.getPsiClassHolder(psiClass0);
                                        apiModelPropertyAtomic.set(psiClassHolder.getCommentInfo());
                                        packageNameUniqueAtomic.set(FileUtil.getPackageNameByPsiClass(psiClass0));
                                    });
                                    CommentInfo commentInfo = apiModelPropertyAtomic.get();
                                    if (commentInfo == null || commentInfo.isHidden(false)) {
                                        continue;
                                    }

                                    String moduleName = getModuleName(packageNameUniqueAtomic, psiClass0, commentInfo);
                                    String fileParentDir = getDirPrefix() + File.separator + moduleName;
                                    File parent = new File(docRootDirPath + File.separator + fileParentDir);
                                    String fileName = getFileName(psiClass0, commentInfo);
                                    String fullFileName = fileName + "." + getFileExtension();
                                    float fraction = i++ / size;
                                    runLoop(project, psiClass0, hasCancelAtomic, commentInfo, moduleName, fileName, parent, fileParentDir, fullFileName, otherMap, indicator, fraction);
                                }
                                runLoopAfter(project, indicator, hasCancelAtomic, finalPsiClassList, docRootDirPath, otherMap);
                                // endregion loop
                            } else {
                                runOnce(project, projectFilePath, docRootDirPath, finalPsiClassList, hasCancelAtomic, indicator);
                            }
                            indicator.setText(getProcessFinishedModelTitle());
                            indicator.setText2(getProcessFinishedModelSubTitle());
                            indicator.setFraction(1f);
                            refreshProject(projectFilePath);
                            Thread.sleep(500);
                        } catch (ProcessCanceledException canceledException) {
                            hasCancelAtomic.set(true);
                            handleCancelTask(docRootDirPath, projectFilePath);
                        } catch (Throwable e1) {
                            hasCancelAtomic.set(true);
                            ActionUtil.handleException(e1);
                        }
                    }
                });
                boolean hasCancel = hasCancelAtomic.get();
                if (!hasCancel) {
                    ClipboardUtil.setSysClipboardText(docRootDirPath);
                    ActionUtil.showDialog(project, getDialogTip(), docRootDirPath);
                }
            }
        } catch (Throwable e1) {
            ActionUtil.handleException(e1);
        } finally {
            ActionUtil.emptyIp();
            destroy(e, project, psiElement);
        }
    }

    protected boolean update0(@NotNull AnActionEvent e, Project project, PsiElement psiElement, PsiClass psiClass, PsiDirectory psiDirectory) {
        return false;
    }

    protected void init(AnActionEvent e, Project project, PsiElement psiElement, PsiClass firstClass) {
        AtomicReference<Map<String, String>> configAtomic = new AtomicReference<>(new HashMap<>());
        ApplicationManager.getApplication().invokeAndWait(() -> {
            PsiFile psiFile = firstClass.getContainingFile();
            configAtomic.set(ConfigUtil.getConfig("docer-config.properties", psiFile));
        });
        Map<String, String> config = configAtomic.get();
        ConfigHolder.putConfig(config);
    }

    protected void destroy(AnActionEvent e, Project project, PsiElement psiElement) {
        ConfigHolder.removeConfig();
    }

    @Nullable
    private PsiClass getFirstPsiClass(@NotNull AnActionEvent e, Project project, PsiClass psiClass, PsiDirectory psiDirectory) {
        PsiClass firstClass = null;
        boolean isRightClickOnClass = psiClass != null;
        if (isRightClickOnClass) {
            firstClass = psiClass;
        } else {
            Set<PsiClass> psiClassList = new TreeSet<>(
                    Comparator.comparing(PsiClass::getQualifiedName)
            );
            psiClassList = getPsiClassList(e, project, psiDirectory, psiClassList);
            if (psiClassList.size() > 0) {
                firstClass = new ArrayList<>(psiClassList).get(0);
            }
        }
        return firstClass;
    }

    private Set<PsiClass> getPsiClassList(@NotNull AnActionEvent e, Project project, PsiDirectory psiDirectory, Set<PsiClass> psiClassList) {
        boolean isRightClickOnDirectory = psiDirectory != null;
        if (isRightClickOnDirectory) {
            handlePsiDirectory(psiDirectory, psiClassList);
        }

        Object data = e.getDataContext().getData("psi.Element.array");
        if (data instanceof PsiElement[]) {
            PsiElement[] psiElements = (PsiElement[]) data;
            if (psiElements.length > 0) {
                for (PsiElement element : psiElements) {
                    if (element instanceof PsiDirectory) {
                        PsiDirectory directory = (PsiDirectory) element;
                        handlePsiDirectory(directory, psiClassList);
                    }
                    if (element instanceof PsiClass) {
                        PsiClass aClass = (PsiClass) element;
                        psiClassList.add(aClass);
                    }
                }
            }
        }
        return filterPsiClassList(psiClassList, project);
    }

    @NotNull
    protected String getDialogTip() {
        return "已生成 Markdown 文档(已过滤部分文件和类), 且文档目录地址已复制到您的剪切板!";
    }

    @NotNull
    protected String getModelTitle() {
        return "努力生成中...";
    }

    @NotNull
    protected String getProcessorModelSubTitle() {
        return "开始生成...";
    }

    @NotNull
    protected String getProcessorModelTitle() {
        return "生成文档中";
    }

    @NotNull
    protected String getProcessFinishedModelSubTitle() {
        return "All done!";
    }

    @NotNull
    protected String getProcessFinishedModelTitle() {
        return "生成完毕!";
    }

    @NotNull
    protected String getFullFileName(PsiClass psiClass0, CommentInfo commentInfo) {
        // 文件名取注解中 tags 或 description, 取注释中第一行非 tag 注释或 @tags/@description 中的值
        return getFileName(psiClass0, commentInfo) + "." + getFileExtension();
    }

    protected String getFileName(PsiClass psiClass0, CommentInfo commentInfo) {
        return commentInfo.getItemName(psiClass0.getName());
    }

    protected String getModuleName(AtomicReference<String> packageNameUniqueAtomic, PsiClass psiClass0, CommentInfo commentInfo) {
        String suffix = "other";
        String packageName = packageNameUniqueAtomic.get();
        if (StringUtils.isNotBlank(packageName)) {
            suffix = packageName;
        }
        if (commentInfo != null) {
            suffix = commentInfo.getSingleStr("module", suffix);
        }
        return FileUtil.getRightFileName(suffix);
    }


    /**
     * 文档目录的固定前缀
     *
     * @return 文档目录的固定前缀
     */
    protected String getDirPrefix() {
        return "doc";
    }

    /**
     * 文件的扩展名
     *
     * @return 文件的扩展名
     */
    @NotNull
    protected String getFileExtension() {
        return "md";
    }

    /**
     * 是否走 for 循环遍历 PsiClass 信息列表来生成
     * 是 走 processorRunByPsiClass()
     * 否 走 processorRunOnce()
     *
     * @return true:是 false:否
     */
    protected boolean isRunLoop() {
        return true;
    }

    /**
     * 只传参, 不做任何辅助操作, 如更新进度/计算文件路径/内容写入到文件等
     *
     * @param project           项目信息
     * @param projectFilePath   项目路径
     * @param docRootDirPath    doc 目录路径
     * @param finalPsiClassList PsiClassList
     * @param hasCancelAtomic   代码是否被取消终止(是则改变此变量中的布尔值)
     * @param indicator         提供进度条能力, 设置主/副标题/进度
     */
    protected void runOnce(Project project, String projectFilePath, String docRootDirPath, Set<PsiClass> finalPsiClassList, AtomicBoolean hasCancelAtomic, ProgressIndicator indicator) throws InterruptedException {

    }

    protected void runLoopBefore(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) {

    }

    protected void runLoopAfter(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) {

    }

    /**
     * 真正的代码, 设置进度条, 生成文件或数据
     *
     * @param project         项目
     * @param psiClass0       类
     * @param hasCancelAtomic 是否中止任务
     * @param commentInfo     类上注释/注解信息
     * @param moduleName      文档模块名称(默认为最后两层报名, 可通过 #module 设置)
     * @param fileName        文件名
     * @param parent          生成的文件目录
     * @param fileParentDir   文档目录路径(文档根目录下相对路径, 由固定前缀{@link #getDirPrefix}+模块名构成)
     * @param fullFileName    生成的文件名(不含目录)
     * @param otherMap        辅助信息
     * @param indicator       进度条
     * @param fraction        for循环进度百分比, 可用于进度条
     */
    protected void runLoop(Project project, PsiClass psiClass0, AtomicBoolean hasCancelAtomic, CommentInfo commentInfo, String moduleName, String fileName, File parent, String fileParentDir, String fullFileName, Map<String, Object> otherMap, ProgressIndicator indicator, float fraction) {
        indicator.setText2("文件写入中：" + API_DOC_ROOT_DIR_NAME + File.separator + fileParentDir + File.separator + fullFileName);
        indicator.setFraction(fraction);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            try {
                String fileContent = runLoop0(psiClass0, project, commentInfo, moduleName, fileName, fullFileName, otherMap);
                if (StringUtils.isNotBlank(fileContent)) {
                    FileUtil.writeStringToFile(fileContent, parent, fullFileName);
                }
            } catch (Exception e1) {
                ActionUtil.handleException(e1);
            }
        });
    }

    /**
     * 根据 psiClass0 信息生成文件内容, 将自动写入到文件
     *
     * @param psiClass0    类信息
     * @param project      项目
     * @param commentInfo  类上的注解/注释信息
     * @param moduleName   模块名称
     * @param fileName     文件名称
     * @param fullFileName 完整文件名称
     * @param otherMap     额外辅助信息
     * @return 文件内容
     */
    protected String runLoop0(PsiClass psiClass0, Project project, CommentInfo commentInfo, String moduleName, String fileName, String fullFileName, Map<String, Object> otherMap) {
        return null;
    }

    /**
     * 处理选中单个类的情况
     *
     * @param project      项目
     * @param psiClass     类信息
     * @param psiClassList 类信息集合(若不想单独处理, add 到此集合接口)
     * @return true: 代表不用继续往下走了 false: 代表需要继续往下走
     */
    protected boolean handleRightClickOnClass(Project project, PsiClass psiClass, Set<PsiClass> psiClassList) {
        psiClassList.add(psiClass);
        return false;
    }

    /**
     * 取消后删除已生成的文件 (考虑先生成到temp, 再覆盖, 避免生成一半把原来的删掉)
     *
     * @param docRootDirPath  doc 路径
     * @param projectFilePath 项目路径
     */
    protected void handleCancelTask(String docRootDirPath, String projectFilePath) {
        File all = new File(docRootDirPath);
        FileUtil.deleteDirectory(all);
        refreshProject(projectFilePath);
    }

    /**
     * 刷新, 等效于 Reload From Disk
     *
     * @param projectFilePath 项目路径
     */
    protected void refreshProject(String projectFilePath) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(projectFilePath));
        if (virtualFile != null) {
            virtualFile.refresh(true, true);
        }
    }

    /**
     * 过滤某些不必要的 class
     *
     * @param psiClassList class 集合
     * @param project      project
     * @return 过滤后的集合
     */
    protected Set<PsiClass> filterPsiClassList(Set<PsiClass> psiClassList, Project project) {
        if (CollectionUtils.isEmpty(psiClassList)) {
            return psiClassList;
        }
        psiClassList.removeIf(next -> !isNeedDealPsiClass(next, project));
        return psiClassList;
    }

    /**
     * 判断 class 是否需要保留
     *
     * @param psiClass class
     * @param project  project
     * @return true 代表保留, false 代表过滤
     */
    protected abstract boolean isNeedDealPsiClass(PsiClass psiClass, Project project);

    /**
     * 先过滤不必要的文件夹, 再将文件下类添加到集合中
     *
     * @param psiDirectory 要处理的文件夹
     * @param psiClassList 集合
     */
    protected void handlePsiDirectory(PsiDirectory psiDirectory, Set<PsiClass> psiClassList) {
        if (psiDirectory == null || psiDirectory.getChildren().length <= 0) {
            return;
        }
        String name = psiDirectory.getName();
        if ("target".equals(name) || "build".equals(name) || name.startsWith(".")) {
            return;
        }
        for (PsiElement child : psiDirectory.getChildren()) {
            if (child instanceof PsiDirectory) {
                PsiDirectory directory = (PsiDirectory) child;
                handlePsiDirectory(directory, psiClassList);
            }
            if (child instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) child;
                PsiClass[] classes = psiJavaFile.getClasses();
                psiClassList.addAll(Arrays.asList(classes));
            }
        }
    }

}