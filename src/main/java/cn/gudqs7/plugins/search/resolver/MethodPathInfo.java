package cn.gudqs7.plugins.search.resolver;

import cn.gudqs7.plugins.common.enums.HttpMethod;
import com.intellij.psi.PsiMethod;
import lombok.Data;

import java.util.List;

/**
 * @author wq
 * @date 2022/5/28
 */
@Data
public class MethodPathInfo {

    private PsiMethod psiMethod;

    private HttpMethod httpMethod;

    private String methodPath;

    private String location;

    private String methodDesc;

    private List<String> params;

    public MethodPathInfo(PsiMethod psiMethod, HttpMethod httpMethod, String methodPath, String location, String methodDesc, List<String> params) {
        this.psiMethod = psiMethod;
        this.httpMethod = httpMethod;
        this.methodPath = methodPath;
        this.location = location;
        this.methodDesc = methodDesc;
        this.params = params;
    }
}
