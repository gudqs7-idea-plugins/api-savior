package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.constant.MapKeyConstant;
import cn.gudqs7.plugins.idea.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.idea.savior.JavaToPostmanSavior;
import cn.gudqs7.plugins.idea.theme.ThemeHelper;
import cn.gudqs7.plugins.idea.util.ActionUtil;
import cn.gudqs7.plugins.idea.util.FileUtil;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author wq
 */
public class PostmanDocerSaviorAction extends AnAction {


    protected JavaToPostmanSavior postmanSavior;

    public PostmanDocerSaviorAction() {
        this.postmanSavior = new JavaToPostmanSavior(ThemeHelper.getRestfulTheme());
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
            if (psiElements.length > 0) {
                return;
            }
        }

        e.getPresentation().setVisible(false);
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            PsiClass psiClass = ActionUtil.getPsiClass(psiElement);
            PsiDirectory psiDirectory = ActionUtil.getPsiDirectory(psiElement);

            Set<PsiClass> psiClassList = new TreeSet<>(
                    Comparator.comparing(PsiClass::getQualifiedName)
            );
            boolean isRightClickOnClass = psiClass != null;
            if (isRightClickOnClass) {
                psiClassList.add(psiClass);
            }

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
            psiClassList = filterPsiClassList(psiClassList, project);
            if (!CollectionUtils.isEmpty(psiClassList)) {
                Set<PsiClass> finalPsiClassList = psiClassList;
                String projectFilePath = project.getBasePath();
                String apiDocPath = projectFilePath + "/api-doc";
                String title = "努力生成中...";
                AtomicBoolean hasCancelOrErrorAtomic = new AtomicBoolean(false);
                ProgressManager.getInstance().run(new Task.Modal(project, title, true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        try {
                            indicator.setIndeterminate(false);
                            indicator.setText("生成中");
                            indicator.setText2("开始...");
                            indicator.setFraction(0.05f);

                            {
                                float i = 1f;
                                int size = finalPsiClassList.size();
                                Map<String, List<Map<String, Object>>> itemListMap = new LinkedHashMap<>(16);
                                AtomicReference<String> hostAndPort = new AtomicReference<>("");
                                for (PsiClass psiClass0 : finalPsiClassList) {
                                    if (indicator.isCanceled()) {
                                        handleCancelTask(apiDocPath, projectFilePath);
                                        hasCancelOrErrorAtomic.set(true);
                                        return;
                                    }
                                    AtomicReference<String> packageNameUniqueAtomic = new AtomicReference<>("");
                                    ApplicationManager.getApplication().invokeAndWait(() -> {
                                        packageNameUniqueAtomic.set(FileUtil.getPackageNameByPsiClass(psiClass0));
                                    });


                                    String suffix = "other";
                                    String packageName = packageNameUniqueAtomic.get();
                                    if (StringUtils.isNotBlank(packageName)) {
                                        suffix = packageName;
                                    }

                                    String fileName = psiClass0.getName();
                                    indicator.setText2("Dealing: ..." + suffix + "." + fileName + ".java");
                                    indicator.setFraction(i++ / size);

                                    String itemKey = suffix;
                                    List<Map<String, Object>> itemList = itemListMap.computeIfAbsent(itemKey, k -> new ArrayList<>(16));
                                    ApplicationManager.getApplication().invokeAndWait(() -> {
                                        try {
                                            Map<String, Object> postmanItem = postmanSavior.generatePostmanItem(psiClass0, project);
                                            if (postmanItem != null) {
                                                Object o = postmanItem.remove(MapKeyConstant.HOST_PORT);
                                                if (o instanceof String) {
                                                    hostAndPort.set((String) o);
                                                }
                                                itemList.add(postmanItem);
                                            }
                                        } catch (Exception e1) {
                                            ActionUtil.handleException(e1);
                                            hasCancelOrErrorAtomic.set(true);
                                        }
                                    });
                                }

                                List<Map<String, Object>> itemList = new ArrayList<>(16);
                                for (Map.Entry<String, List<Map<String, Object>>> entry : itemListMap.entrySet()) {
                                    String key = entry.getKey();
                                    List<Map<String, Object>> list = entry.getValue();
                                    if (!CollectionUtils.isEmpty(list)) {
                                        Map<String, Object> item = new LinkedHashMap<>(4);
                                        item.put("name", key);
                                        item.put("item", list);

                                        itemList.add(item);
                                    }
                                }

                                String projectName = project.getName();
                                Map<String, Object> postmanObj = new LinkedHashMap<>(8);
                                Map<String, Object> info = new LinkedHashMap<>(8);
                                info.put("_postman_id", DigestUtils.md5Hex(projectName));
                                info.put("name", projectName);
                                info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
                                Map<String, Object> variable = new LinkedHashMap<>(8);
                                variable.put("key", projectName + "-url");
                                variable.put("value", hostAndPort.get());

                                postmanObj.put("info", info);
                                postmanObj.put("item", itemList);
                                postmanObj.put("variable", Collections.singletonList(variable));
                                String json = postmanSavior.getTheme().formatJson(postmanObj, RequestMapping.ContentType.APPLICATION_JSON);

                                File parent = new File(apiDocPath + File.separator + "postman");
                                FileUtil.writeStringToFile(json, parent, projectName + ".postman_collection.json");

                                indicator.setText2("导出成功!");
                                indicator.setText2("All done!");
                                indicator.setFraction(1f);
                                refreshProject(projectFilePath);
                                Thread.sleep(1000);
                            }
                        } catch (ProcessCanceledException canceledException) {
                            handleCancelTask(apiDocPath, projectFilePath);
                            hasCancelOrErrorAtomic.set(true);
                        } catch (Throwable e1) {
                            hasCancelOrErrorAtomic.set(true);
                            ActionUtil.handleException(e1);
                        }
                    }
                });
                boolean hasCancelOrError = hasCancelOrErrorAtomic.get();
                if (!hasCancelOrError) {
                    postmanSavior.setSysClipboardText(apiDocPath);
                    ActionUtil.showDialog(project, "导出成功(已过滤部分文件和类), 且对应文件目录地址已复制到您的剪切板!", apiDocPath);
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
        return psiClassList.stream().filter(psiClass -> isNeedDealPsiClass(psiClass, project)).collect(Collectors.toSet());
    }

    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        // controller 和 feign 处理, 其他不处理
        PsiAnnotation psiAnnotation = psiClass.getAnnotation("org.springframework.stereotype.Controller");
        if (psiAnnotation == null) {
            psiAnnotation = psiClass.getAnnotation("org.springframework.web.bind.annotation.RestController");
        }
        // 若类不是 Controller 则不显示
        if (psiAnnotation != null) {
            return true;
        }
        boolean isFeignClient = psiClass.getAnnotation("org.springframework.cloud.openfeign.FeignClient") != null;
        if (isFeignClient) {
            return true;
        }

        return false;
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
