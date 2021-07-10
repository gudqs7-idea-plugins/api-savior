package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.idea.savior.JavaToDocSavior;
import cn.gudqs7.plugins.idea.util.ActionUtil;
import cn.gudqs7.plugins.idea.util.AnnotationHolder;
import cn.gudqs7.plugins.idea.util.FileUtil;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationType;
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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author wq
 */
public abstract class AbstractProjectDocerSavior extends AnAction {

    protected JavaToDocSavior docSavior;

    public AbstractProjectDocerSavior(JavaToDocSavior docSavior) {
        this.docSavior = docSavior;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        PsiClass psiClass = ActionUtil.getPsiClass(psiElement);
        PsiDirectory psiDirectory = ActionUtil.getPsiDirectory(psiElement);

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
            if (psiElements != null && psiElements.length > 0) {
                return;
            }
        }

        e.getPresentation().setVisible(false);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            PsiClass psiClass = ActionUtil.getPsiClass(psiElement);
            PsiDirectory psiDirectory = ActionUtil.getPsiDirectory(psiElement);

            boolean isRightClickOnClass = psiClass != null;
            if (isRightClickOnClass) {
                if (isNeedDealPsiClass(psiClass, project)) {
                    String docByInterface = docSavior.generateApiByServiceInterface(psiClass, project);
                    docSavior.setSysClipboardText(docByInterface);
                    ActionUtil.showDialog(project, "已自动的将 Markdown 文档复制到您的剪切板!\n您可在此预览后再去粘贴!", docByInterface);
                    return;
                }
            }

            Set<PsiClass> psiClassList = new HashSet<>(32);
            boolean isRightClickOnDirectory = psiDirectory != null;
            if (isRightClickOnDirectory) {
                handlePsiDirectory(psiDirectory, psiClassList);
            }

            Object data = e.getDataContext().getData("psi.Element.array");
            if (data instanceof PsiElement[]) {
                PsiElement[] psiElements = (PsiElement[]) data;
                if (psiElements != null && psiElements.length > 0) {
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
            psiClassList = filterPsiClassList(psiClassList, project);
            if (!CollectionUtils.isEmpty(psiClassList)) {
                Set<PsiClass> finalPsiClassList = psiClassList;
                String projectFilePath = project.getBasePath();
                String apiDocPath = projectFilePath + "/api-doc";
                String title = "努力生成中...";
                AtomicBoolean hasCancelAtomic = new AtomicBoolean(false);
                ProgressManager.getInstance().run(new Task.Modal(project, title, true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        try {
                            indicator.setIndeterminate(false);
                            indicator.setText("生成文档中");
                            indicator.setText2("开始生成...");
                            indicator.setFraction(0.05f);

                            {
                                float i = 1f;
                                int size = finalPsiClassList.size();
                                for (PsiClass psiClass0 : finalPsiClassList) {
                                    if (indicator.isCanceled()) {
                                        handleCancelTask(apiDocPath, projectFilePath);
                                        hasCancelAtomic.set(true);
                                        return;
                                    }
                                    AnnotationHolder psiClassHolder = AnnotationHolder.getPsiClassHolder(psiClass0);
                                    AtomicReference<ApiModelProperty> apiModelPropertyAtomic = new AtomicReference<>(null);
                                    AtomicReference<String> packageNameUniqueAtomic = new AtomicReference<>("");
                                    ApplicationManager.getApplication().invokeAndWait(() -> {
                                        apiModelPropertyAtomic.set(psiClassHolder.getApiModelProperty());
                                        packageNameUniqueAtomic.set(FileUtil.getPackageNameByPsiClass(psiClass0));
                                    });
                                    // 文件名取注解中 tags 或 description, 取注释中第一行非 tag 注释或 @tags/@description 中的值
                                    ApiModelProperty apiModelProperty = apiModelPropertyAtomic.get();
                                    if (apiModelProperty == null || apiModelProperty.isHidden(false)) {
                                        continue;
                                    }

                                    String fileName = psiClass0.getName() + ".md";
                                    String tags = apiModelProperty.getTags("");
                                    if (StringUtils.isNotBlank(tags)) {
                                        fileName = tags + ".md";
                                    } else {
                                        String value = apiModelProperty.getValue("");
                                        if (StringUtils.isNotBlank(value)) {
                                            fileName = value + ".md";
                                        }
                                    }

                                    String suffix = "other";
                                    String packageName = packageNameUniqueAtomic.get();
                                    if (StringUtils.isNotBlank(packageName)) {
                                        suffix = packageName;
                                    }
                                    suffix = File.separator + getFileNamePrefix() + File.separator + FileUtil.getRightFileName(suffix);

                                    File parent = new File(apiDocPath + suffix);
                                    indicator.setText2("Writing: api-doc" + suffix + fileName);
                                    indicator.setFraction(i++ / size);

                                    String finalFileName = fileName;
                                    ApplicationManager.getApplication().invokeAndWait(() -> {
                                        try {
                                            String docByInterface = docSavior.generateApiByServiceInterface(psiClass0, project);
                                            if (StringUtils.isNotBlank(docByInterface)) {
                                                FileUtil.writeStringToFile(docByInterface, parent, finalFileName);
                                            }
                                        } catch (Exception e1) {
                                            ActionUtil.handleException(e1);
                                        }
                                    });
                                }
                                indicator.setText2("生成完毕!");
                                indicator.setText2("All done!");
                                indicator.setFraction(1f);
                                refreshProject(projectFilePath);
                                Thread.sleep(1000);
                            }
                        } catch (ProcessCanceledException canceledException) {
                            handleCancelTask(apiDocPath, projectFilePath);
                            hasCancelAtomic.set(true);
                        } catch (Throwable e1) {
                            ActionUtil.handleException(e1);
                        }
                    }
                });
                boolean hasCancel = hasCancelAtomic.get();
                if (!hasCancel) {
                    docSavior.setSysClipboardText(apiDocPath);
                    ActionUtil.showDialog(project, "已生成 Markdown 文档(已过滤部分文件和类), 且文档目录地址已复制到您的剪切板!", apiDocPath);
                }
            }
        } catch (Throwable e1) {
            ActionUtil.handleException(e1);
        } finally {
            ActionUtil.emptyIp();
        }
    }

    private void handleCancelTask(String apiDocPath, String projectFilePath) {
        File all = new File(apiDocPath);
        FileUtil.deleteDirectory(all);
        refreshProject(projectFilePath);
    }

    private void refreshProject(String projectFilePath) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(projectFilePath));
        if (virtualFile != null) {
            virtualFile.refresh(true, true);
        }
    }

    /**
     * 过滤某些不必要的 class
     *
     * @param psiClassList
     * @return
     */
    protected Set<PsiClass> filterPsiClassList(Set<PsiClass> psiClassList, Project project) {
        if (CollectionUtils.isEmpty(psiClassList)) {
            return psiClassList;
        }
        Set<PsiClass> classList = psiClassList.stream().filter(psiClass -> {
            return isNeedDealPsiClass(psiClass, project);
        }).collect(Collectors.toSet());
        return classList;
    }

    /**
     * 判断 class 是否需要保留
     *
     * @param psiClass
     * @param project
     * @return true 代表保留, false 代表过滤
     */
    protected abstract boolean isNeedDealPsiClass(PsiClass psiClass, Project project);

    /**
     * 文件的前缀
     *
     * @return
     */
    protected String getFileNamePrefix() {
        return "doc";
    }

    protected void handlePsiDirectory(PsiDirectory psiDirectory, Set<PsiClass> psiClassList) {
        if (psiDirectory == null || psiDirectory.getChildren() == null || psiDirectory.getChildren().length <= 0) {
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
                if (classes != null || classes.length > 0) {
                    psiClassList.addAll(Arrays.asList(classes));
                }
            }
        }
    }

}
