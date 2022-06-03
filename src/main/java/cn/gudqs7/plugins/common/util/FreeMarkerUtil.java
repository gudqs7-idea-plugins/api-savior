package cn.gudqs7.plugins.common.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * freemarker 模版渲染工具类
 * @author wenquan
 * @date 2022/4/19
 */
public class FreeMarkerUtil {

    public static String renderTemplate(String templateName, Map<String, Object> root) {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
            cfg.setClassLoaderForTemplateLoading(FreeMarkerUtil.class.getClassLoader(), "template/ftl");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

            Template temp = cfg.getTemplate(templateName);
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream(4096);
            Writer out = new OutputStreamWriter(arrayOutputStream);
            temp.process(root, out);
            byte[] bytes = arrayOutputStream.toByteArray();
            //noinspection StringOperationCanBeSimplified
            return new String(bytes);
        } catch (Exception e) {
            ActionUtil.handleException(e);
        }
        return null;
    }

}
