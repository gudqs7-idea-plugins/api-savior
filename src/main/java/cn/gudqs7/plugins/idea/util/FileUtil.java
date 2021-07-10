package cn.gudqs7.plugins.idea.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author wq
 */
public class FileUtil {

    public static void writeStringToFile(String content, String path) {

    }

    public static String getRightFileName(String fileName) {
        fileName = fileName.replaceAll("\\\\", "");
        fileName = fileName.replaceAll("/", "");
        fileName = fileName.replaceAll(":", "");
        fileName = fileName.replaceAll("\\*", "");
        fileName = fileName.replaceAll("\\?", "");
        fileName = fileName.replaceAll("\"", "");
        fileName = fileName.replaceAll("\\<", "");
        fileName = fileName.replaceAll("\\>", "");
        fileName = fileName.replaceAll("\\|", "");
        return fileName;
    }

    public static void writeStringToFile(String content, File parent, String path) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(path)) {
            return;
        }
        path = getRightFileName(path);
        if (parent == null || parent.isFile()) {
            return;
        }
        try {
            if (!parent.exists()) {
                parent.mkdirs();
            }
            File file = new File(parent, path);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("writeStringToFile Error: " + e.toString());
        }
    }

    public static void deleteDirectory(File file) {
        try {
            if (file != null && file.exists() && file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            }
        } catch (Exception e) {
            throw new RuntimeException("deleteDirectory Error: " + e.toString());
        }
    }

    public static String getPackageNameByPsiClass(PsiClass psiClass0) {
        String packageNameUnique = "";
        PsiElement element = psiClass0.getParent();
        if (element instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) element;
            String packageName = psiJavaFile.getPackageName();
            if (StringUtils.isNotBlank(packageName)) {
                String[] packageArray = packageName.split("\\.");
                int length = packageArray.length;
                if (length > 0) {
                    if (length == 1) {
                        return packageArray[0];
                    }
                    if (length > 1) {
                        return packageArray[length - 2] + "." + packageArray[length - 1];
                    }
                } else {
                    return packageName;
                }
            }
        }
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            return getPackageNameByPsiClass(psiClass);
        }
        return packageNameUnique;
    }

}
