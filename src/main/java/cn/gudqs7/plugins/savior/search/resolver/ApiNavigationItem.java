package cn.gudqs7.plugins.savior.search.resolver;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wq
 * @date 2022/5/28
 */
@Data
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
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

    public String getRightText() {
        if (StringUtils.isNotBlank(methodPathInfo.getMethodDesc())) {
            return methodPathInfo.getLocation() + "#" + methodPathInfo.getMethodDesc();
        } else {
            return methodPathInfo.getLocation();
        }
    }

}
