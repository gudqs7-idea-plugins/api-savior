package cn.gudqs7.plugins.common.util.structure;

import cn.gudqs7.plugins.common.util.jetbrain.PsiSearchUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author wenquan
 * @date 2021/9/30
 */
public class PackageInfoUtil {

    /**
     * 获取Java文件中包上的注解信息
     *
     * @param psiJavaFile    Java文件
     * @param annotationName 注解全限定名
     * @return 注解信息
     */
    public static PsiAnnotation getPackageAnnotation(PsiJavaFile psiJavaFile, String annotationName) {
        PsiPackageStatement packageStatement = psiJavaFile.getPackageStatement();
        if (packageStatement != null) {
            PsiModifierList annotationList = packageStatement.getAnnotationList();
            if (annotationList != null) {
                PsiAnnotation[] annotations = annotationList.getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    if (Objects.equals(annotation.getQualifiedName(), annotationName)) {
                        return annotation;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取Java文件的第一份注释(包注释)
     *
     * @param psiJavaFile Java 文件
     * @return 第一份注释
     */
    public static PsiComment getPackageComment(PsiJavaFile psiJavaFile) {
        if (psiJavaFile != null) {
            for (PsiElement child : psiJavaFile.getChildren()) {
                if (child instanceof PsiComment) {
                    return (PsiComment) child;
                }
            }
        }
        return null;
    }

    /**
     * 获取注释中的指定 tag 的值
     *
     * @param psiComment 注释
     * @param tagName    指定 tag
     * @return tag 的值
     */
    public static String getCommentTagByPsiComment(PsiComment psiComment, String tagName) {
        if (psiComment != null) {
            String text = psiComment.getText();
            if (text.startsWith("/**") && text.endsWith("*/")) {
                String[] lines = text.replaceAll("\r", "").split("\n");
                for (String line : lines) {
                    if (line.contains("/**") || line.contains("*/")) {
                        continue;
                    }
                    line = line.replaceAll("\\*", "").trim();
                    if (StringUtils.isBlank(line)) {
                        continue;
                    }
                    if (line.contains("@") || line.contains("#")) {
                        String[] tagValArray = line.split(" ");
                        String tag = "";
                        String tagVal = null;
                        if (tagValArray.length > 0) {
                            tag = tagValArray[0].trim();
                        }
                        if (tagValArray.length > 1) {
                            tagVal = line.substring(tag.length()).trim();
                        }
                        tag = tag.substring(1);
                        if (Objects.equals(tag, tagName)) {
                            return tagVal;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取指定包下的 package-info.java 类
     *
     * @param project     项目
     * @param packageName 包名
     * @return Java类信息
     */
    public static PsiJavaFile getPackageInfoFile(Project project, String packageName) {
        return PsiSearchUtil.searchPsiJavaFileWithPackage(project, packageName, "package-info.java");
    }
}
