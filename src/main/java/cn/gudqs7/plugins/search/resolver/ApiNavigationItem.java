package cn.gudqs7.plugins.search.resolver;

import cn.gudqs7.plugins.util.IconUtil;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author wq
 * @date 2022/5/28
 */
@Data
public class ApiNavigationItem implements NavigationItem {

    private Navigatable navigationElement;
    private PsiElement psiElement;
    private PsiMethod psiMethod;

    private HttpMethod httpMethod;
    private String url;

    public ApiNavigationItem(PsiElement psiElement, HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.psiElement = psiElement;
        if (psiElement instanceof PsiMethod) {
            this.psiMethod = (PsiMethod) psiElement;
        }
        if (psiElement instanceof Navigatable) {
            this.navigationElement = (Navigatable) psiElement;
        }
    }

    @Override
    public String getName() {
        return url;
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return new ApiItemPresentation();
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (navigationElement != null) {
            navigationElement.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return navigationElement != null && navigationElement.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return navigationElement != null && navigationElement.canNavigateToSource();
    }

    private class ApiItemPresentation implements ItemPresentation {

        @Nullable
        @Override
        public String getPresentableText() {
            return url;
        }

        @Nullable
        @Override
        public String getLocationString() {
            if (psiMethod != null) {
                return psiMethod.getContainingClass().getName().concat("#").concat(psiMethod.getName());
            }
            return "unknownLocation";
        }

        @Nullable
        @Override
        public Icon getIcon(boolean unused) {
            return IconUtil.getHttpMethodIcon(httpMethod);
        }
    }

}
