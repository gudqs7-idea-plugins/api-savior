package cn.gudqs7.plugins.common.util.file;

import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author wenquan
 * @date 2022/8/11
 */
public class PdfUtil {

    public static void html2Pdf(String html, File parent, String pdfFileName) {
        try {
            FileOutputStream outputStream = FileUtil.getFileOutputStream(parent, pdfFileName);
            if (outputStream == null) {
                return;
            }

            Document document = Jsoup.parse(html, "UTF-8");
            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
            org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(document);

            InputStream fontStream = PdfUtil.class.getClassLoader().getResourceAsStream("fonts/msyh.ttf");
            File fontFile = FileUtil.generateTempFile(fontStream, "pdfFont", ".ttf");
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.toStream(outputStream);
            builder.useFont(fontFile, "Microsoft YaHei", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.withW3cDocument(w3cDoc, null);
            builder.run();

            outputStream.close();
        } catch (Throwable e) {
            ExceptionUtil.handleException(e);
        }
    }

}
