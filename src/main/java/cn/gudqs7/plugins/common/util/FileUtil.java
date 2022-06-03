package cn.gudqs7.plugins.common.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author wq
 */
public class FileUtil {

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
        if (parent == null || !parent.isDirectory()) {
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
            fileOutputStream.write(content.getBytes());
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

}
