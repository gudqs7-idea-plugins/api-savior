package cn.gudqs7.plugins.docer.savior;

import cn.gudqs7.plugins.docer.theme.Theme;
import cn.gudqs7.plugins.util.PsiClassUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.Arrays;
import java.util.Map;

/**
 * @author wq
 * @date 2021/5/19
 */
public class JavaToDocSavior extends AbstractDocSavior {

    public JavaToDocSavior(Theme theme) {
        super(theme);
    }

    public String generateApiByServiceInterface(PsiClass psiClass, Project project) {
        String interfaceClassName = psiClass.getQualifiedName();
        PsiMethod[] methods = PsiClassUtil.getAllMethods(psiClass);
        // 根据 @Order 注解 以及字母顺序, 从小到大排序
        Arrays.sort(methods, this::orderByMethod);

        StringBuilder allDoc = new StringBuilder();
        for (PsiMethod method : methods) {
            if (filterMethod(method)){
                continue;
            }

            String doc = generateDocByMethod(project, interfaceClassName, method, false);
            allDoc.append(doc);
        }
        return allDoc.toString();
    }

    public String generateDocByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, boolean jumpHidden) {
        Map<String, String> data = getData(project, interfaceClassName, publicMethod, jumpHidden);
        if (data == null) {
            return "";
        }
        String template = getTemplate(theme.getMethodPath(), data);
        return template+"\n\n";
    }

}
