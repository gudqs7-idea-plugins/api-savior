package cn.gudqs7.plugins.search.resolver;

import cn.gudqs7.plugins.common.enums.HttpMethod;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 * @date 2022/5/28
 */
@Data
public class ApiNavigationItem {

    private PsiElement psiElement;
    private PsiMethod psiMethod;

    private HttpMethod httpMethod;
    private String url;
    private MethodPathInfo methodPathInfo;

    public ApiNavigationItem(PsiElement psiElement, HttpMethod httpMethod, String url, MethodPathInfo methodPathInfo) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.methodPathInfo = methodPathInfo;
        this.psiElement = psiElement;
        if (psiElement instanceof PsiMethod) {
            this.psiMethod = (PsiMethod) psiElement;
        }
    }

    @NotNull
    public String getRightText() {
        if (StringUtils.isNotBlank(methodPathInfo.getMethodDesc())) {
            return methodPathInfo.getMethodDesc() + " " + methodPathInfo.getLocation();
        } else {
            return methodPathInfo.getLocation();
        }
    }

    @Override
    public String toString() {
        return getRightText();
    }
}
