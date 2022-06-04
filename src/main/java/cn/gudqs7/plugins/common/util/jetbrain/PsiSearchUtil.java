package cn.gudqs7.plugins.common.util.jetbrain;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Psi 搜索工具类
 *
 * @author wq
 * @date 2022/6/4
 */
public class PsiSearchUtil {

    /**
     * 根据文件名和过滤器查询一个符合条件的文件(仅搜索项目范围)
     *
     * @param project        项目
     * @param searchFileName 文件名
     * @param fileFilterFn   过滤器
     * @return 文件
     */
    @Nullable
    public static <T extends PsiFile> T searchPsiFileByName(@NotNull Project project, String searchFileName, Function<PsiFile, Boolean> fileFilterFn) {
        return searchPsiFileByName(project, searchFileName, false, fileFilterFn);
    }

    /**
     * 根据文件名和过滤器查询一个符合条件的文件
     *
     * @param <T>            具体返回类型
     * @param project        项目
     * @param searchFileName 文件名
     * @param searchAll      是否搜索所有范围, 否则为项目范围
     * @param fileFilterFn   过滤器
     * @return 文件
     */
    @Nullable
    public static <T extends PsiFile> T searchPsiFileByName(@NotNull Project project, String searchFileName, boolean searchAll, Function<PsiFile, Boolean> fileFilterFn) {
        GlobalSearchScope searchScope = getSearchScope(project, searchAll);
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, searchFileName, searchScope);
        for (PsiFile psiFile : psiFiles) {
            if (fileFilterFn.apply(psiFile)) {
                return (T) psiFile;
            }
        }
        return null;
    }

    /**
     * 根据全限定名搜索 PsiClass(仅搜索项目范围)
     *
     * @param project            项目
     * @param qualifiedClassName 全限定名
     * @return PsiClass 对象
     */
    public static PsiClass findPsiClassByQname(Project project, String qualifiedClassName) {
        return findPsiClassByQname(project, qualifiedClassName, false);
    }

    /**
     * 根据全限定名搜索 PsiClass
     *
     * @param project            项目
     * @param qualifiedClassName 全限定名
     * @param searchAll          是否搜索所有范围
     * @return PsiClass 对象
     */
    public static PsiClass findPsiClassByQname(Project project, String qualifiedClassName, boolean searchAll) {
        GlobalSearchScope searchScope = getSearchScope(project, searchAll);
        return JavaPsiFacade.getInstance(project).findClass(qualifiedClassName, searchScope);
    }


    /**
     * 根据文件名获取指定包下的某 Java 文件
     *
     * @param project     项目
     * @param packageName 包名
     * @param fileName    文件名
     * @return Java类信息
     */
    public static PsiJavaFile searchPsiJavaFileWithPackage(Project project, String packageName, String fileName) {
        return searchPsiFileByName(project, fileName, psiFile -> {
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                return psiJavaFile.getPackageName().equals(packageName);
            }
            return false;
        });
    }

    @NotNull
    private static GlobalSearchScope getSearchScope(@NotNull Project project, boolean searchAll) {
        GlobalSearchScope searchScope;
        if (searchAll) {
            searchScope = GlobalSearchScope.allScope(project);
        } else {
            searchScope = GlobalSearchScope.projectScope(project);
        }
        return searchScope;
    }

}
