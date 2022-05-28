package cn.gudqs7.plugins.search.resolver;

import com.intellij.psi.PsiMethod;
import lombok.Data;

/**
 * @author wq
 * @date 2022/5/28
 */
@Data
public class MethodPathInfo {

    private PsiMethod psiMethod;

    private HttpMethod httpMethod;

    private String methodPath;

    public MethodPathInfo(PsiMethod psiMethod, HttpMethod httpMethod, String methodPath) {
        this.psiMethod = psiMethod;
        this.httpMethod = httpMethod;
        this.methodPath = methodPath;
    }
}
