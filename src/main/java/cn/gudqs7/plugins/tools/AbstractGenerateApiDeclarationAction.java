package cn.gudqs7.plugins.tools;

import cn.gudqs7.plugins.common.base.action.intention.AbstractEditorIntentionAction;
import cn.gudqs7.plugins.common.util.jetbrain.NotificationUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiDocumentUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiSearchUtil;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author wenquan
 * @date 2022/8/19
 */
public abstract class AbstractGenerateApiDeclarationAction extends AbstractEditorIntentionAction {

    public static final String PACKAGE_NAME_CONTROLLER = "controller";

    /**
     * 模板名称
     */
    private final String templateName;

    /**
     * 是否生成响应类
     */
    private final boolean generateResponseClass;

    public AbstractGenerateApiDeclarationAction(String templateName) {
        this(templateName, false);
    }

    public AbstractGenerateApiDeclarationAction(String templateName, boolean generateResponseClass) {
        this.templateName = templateName;
        this.generateResponseClass = generateResponseClass;
    }

    @Override
    protected boolean isAvailable0(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws Throwable {
        PsiElement parent = element.getParent();
        if (parent instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) parent;
            boolean controllerOrFeign = PsiClassUtil.isControllerOrFeign(psiClass);
            if (!controllerOrFeign) {
                return false;
            }
        }
        if (element instanceof PsiWhiteSpace) {
            PsiElement prevSibling = element.getPrevSibling();
            if (prevSibling instanceof PsiErrorElement) {
                PsiElement typeElement = prevSibling.getPrevSibling();
                return typeElement instanceof PsiTypeElement;
            }
            return prevSibling instanceof PsiTypeElement;
        }
        return false;
    }

    @Override
    protected void invoke0(Project project, Editor editor, PsiElement element, Document elementDocument, PsiDocumentManager psiDocumentManager) throws Throwable {
        if (element instanceof PsiWhiteSpace) {
            PsiElement prevSibling = element.getPrevSibling();
            if (prevSibling instanceof PsiErrorElement) {
                PsiElement typeElement = prevSibling.getPrevSibling();
                if (typeElement instanceof PsiTypeElement) {
                    PsiTypeElement psiTypeElement = (PsiTypeElement) typeElement;
                    invokeByTypeElement(project, editor, element, psiTypeElement);
                }
            }
            if (prevSibling instanceof PsiTypeElement) {
                PsiTypeElement psiTypeElement = (PsiTypeElement) prevSibling;
                invokeByTypeElement(project, editor, element, psiTypeElement);
            }
        }
    }

    private void invokeByTypeElement(Project project, Editor editor, PsiElement element, PsiTypeElement psiTypeElement) throws IOException {
        String packageName = getJavaFilePackageName(element);
        if (StringUtils.isBlank(packageName)) {
            return;
        }
        PsiFile containingFile = element.getContainingFile();
        String[] packageArray = packageName.split("\\.");
        int length = packageArray.length;
        String lastWord = packageArray[length - 1];
        String lastLastWord = packageArray[length - 2];

        String moduleName = null;
        if (PACKAGE_NAME_CONTROLLER.equals(lastWord)) {
            moduleName = lastLastWord;
        } else if (PACKAGE_NAME_CONTROLLER.equals(lastLastWord)) {
            moduleName = lastWord;
        }
        if (moduleName == null) {
            return;
        }
        PsiDirectory projectDirectory = getProjectDirectory(project);
        if (projectDirectory == null) {
            return;
        }

        String methodName = psiTypeElement.getText();
        String[] wordArray = StringUtils.splitByCharacterTypeCamelCase(methodName);
        String[] wordArray0 = new String[wordArray.length - 1];
        System.arraycopy(wordArray, 1, wordArray0, 0, wordArray0.length);

        String thingName = StringUtils.join(wordArray0);
        String requestClass = methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + "Request";
        String requestClassVar = methodName + "Request";
        String responseClass = thingName.substring(0, 1).toUpperCase() + thingName.substring(1) + "Response";

        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(project);
        FileTemplate apiTemplate = fileTemplateManager.getPattern(templateName);
        if (apiTemplate == null) {
            NotificationUtil.showTips("需在 File and Templates -> Includes 创建名为" + templateName + "的方法定义模版.");
            return;
        }
        Map<String, String> attributes = new HashMap<>(32);
        attributes.put("methodName", methodName);
        attributes.put("serviceName", getServiceName(element, psiTypeElement, project));
        attributes.put("requestClass", requestClass);
        attributes.put("requestClassVar", requestClassVar);
        attributes.put("responseClass", responseClass);
        String templateText = apiTemplate.getText(attributes);

        Set<String> importSet = new HashSet<>(8);

        // 寻找 dto 包的目录所在, 并通过模版创建两个 DTO.
        String packagePrefix = StringUtils.join(packageArray, ".", 0, length - 2);
        String dstPackage = packagePrefix + ".business." + moduleName;
        String dstDir = dstPackage.replaceAll("\\.", "/");
        PsiDirectory businessDirectory = findDirectory(projectDirectory, dstDir);
        if (businessDirectory == null) {
            return;
        }

        PsiDirectory requestDir = findDirectory(businessDirectory, "dto/request");
        PsiDirectory responseDir = findDirectory(businessDirectory, "dto/response");

        String requestClassQn = dstPackage + ".dto.request." + requestClass;
        String responseClassQn = dstPackage + ".dto.response." + responseClass;

        importSet.add(requestClassQn);
        PsiClass requestClassByQn = PsiSearchUtil.findPsiClassByQname(project, requestClassQn);
        if (requestClassByQn == null) {
            if (requestDir != null) {
                String requestTemplateName = "ApiRequest";
                if (templateNotExists(fileTemplateManager, requestTemplateName)) {
                    return;
                }
                HashMap<String, String> additionalProperties = new HashMap<>(32);
                JavaDirectoryService.getInstance().createClass(requestDir, requestClass, requestTemplateName, false, additionalProperties);
            }
        }
        if (generateResponseClass) {
            importSet.add(responseClassQn);
            PsiClass responseClassByQn = PsiSearchUtil.findPsiClassByQname(project, responseClassQn);
            if (responseClassByQn == null) {
                if (responseDir != null) {
                    String responseTemplateName = "ApiResponse";
                    if (templateNotExists(fileTemplateManager, responseTemplateName)) {
                        return;
                    }
                    HashMap<String, String> additionalProperties = new HashMap<>(32);
                    JavaDirectoryService.getInstance().createClass(responseDir, responseClass, responseTemplateName, false, additionalProperties);
                }
            }
        }

        // 移除 方法名 这段代码
        Document document = editor.getDocument();
        document.deleteString(psiTypeElement.getTextRange().getStartOffset(), psiTypeElement.getTextRange().getEndOffset());

        // 通过 Template 将代码写入到 Document, 以及将 import 语句置入
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        PsiDocumentUtil.startTemplate(templateText, editor, containingFile, (Variable[]) null);
        PsiDocumentUtil.addImportToFile(psiDocumentManager, (PsiJavaFile) containingFile, document, importSet);
    }

    private boolean templateNotExists(FileTemplateManager fileTemplateManager, String templateName) {
        FileTemplate responseTemplate = fileTemplateManager.getTemplate(templateName);
        if (responseTemplate == null) {
            NotificationUtil.showTips("需在 File and Templates -> Includes 创建名为" + templateName + "的方法定义模版.");
            return true;
        }
        return false;
    }

    @NotNull
    private String getServiceName(PsiElement element, PsiTypeElement psiTypeElement, Project project) {
        PsiClass currentPsiClass = getCurrentPsiClass(element);
        if (currentPsiClass != null) {
            PsiField[] allFields = currentPsiClass.getAllFields();
            if (ArrayUtils.isNotEmpty(allFields)) {
                for (PsiField field : allFields) {
                    String fieldName = field.getName();
                    if (fieldName.endsWith("Service") && (field.hasAnnotation("javax.annotation.Resource") || field.hasAnnotation("org.springframework.beans.factory.annotation.Autowired"))) {
                        return fieldName;
                    }
                }
            }
        }
        return "xxxService";
    }

    private PsiDirectory getProjectDirectory(Project project) {
        String basePath = project.getBasePath();
        if (basePath != null) {
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(basePath));
            if (virtualFile != null) {
                return PsiDirectoryFactory.getInstance(project).createDirectory(virtualFile);
            }
        }
        return null;
    }

    private String getJavaFilePackageName(PsiElement element) {
        PsiClass currentPsiClass = getCurrentPsiClass(element);
        if (currentPsiClass != null) {
            return PsiClassUtil.getPackageName(currentPsiClass);
        }
        return null;
    }

    private PsiClass getCurrentPsiClass(PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof PsiClass) {
            return (PsiClass) parent;
        }
        return null;
    }

    private PsiDirectory findDirectory(PsiDirectory psiDirectory, String dstDir) {
        String name = psiDirectory.getName();
        if ("target".equals(name) || "build".equals(name) || name.startsWith(".")) {
            return null;
        }
        if (psiDirectory.getVirtualFile().getPath().contains(dstDir)) {
            return psiDirectory;
        }
        for (PsiDirectory subdirectory : psiDirectory.getSubdirectories()) {
            PsiDirectory businessDirectory = findDirectory(subdirectory, dstDir);
            if (businessDirectory != null) {
                return businessDirectory;
            }
        }
        return null;
    }

}
