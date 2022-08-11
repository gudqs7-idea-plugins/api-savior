package cn.gudqs7.plugins.common.util.file;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
        if (StringUtils.isBlank(content)) {
            return;
        }
        try {
            FileOutputStream fileOutputStream = getFileOutputStream(parent, path);
            if (fileOutputStream == null) {
                return;
            }
            // 此处编码应与 FreeMarker 设置的编码以及文件编码统一, 因此需要指定, 与默认编码无关
            fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("writeStringToFile Error: " + e.toString());
        }
    }

    public static FileOutputStream getFileOutputStream(File parent, String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        path = getRightFileName(path);
        // 若 parent 不存在, 则 parent.isFile() 返回 false, 不影响代码运行 (反之判断是否为目录则有问题)
        if (parent == null || parent.isFile()) {
            return null;
        }
        try {
            if (!parent.exists()) {
                parent.mkdirs();
            }
            File file = new File(parent, path);
            if (!file.exists()) {
                file.createNewFile();
            }
            return new FileOutputStream(file);
        } catch (Exception e) {
            throw new RuntimeException("getFileOutputStream Error - " + e.getMessage());
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

    @SneakyThrows(IOException.class)
    public static File generateTempFile(InputStream fontStream, String prefix, String suffix) {
        if (fontStream == null) {
            return null;
        }
        File pdfFont = File.createTempFile(prefix, suffix);
        FileUtils.copyToFile(fontStream, pdfFont);
        return pdfFont;
    }

}
