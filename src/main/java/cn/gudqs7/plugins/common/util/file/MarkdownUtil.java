package cn.gudqs7.plugins.common.util.file;

import com.youbenzi.mdtool.markdown.Analyzer;
import com.youbenzi.mdtool.markdown.bean.Block;

import java.util.List;

/**
 * markdown 相关工具
 *
 * @author wq
 * @date 2022/6/6
 */
public class MarkdownUtil {

    public static String markdownToHtml(String markdown) {
        if(markdown==null){
            return null;
        }

        List<Block> list = Analyzer.analyze(markdown);
        HTMLDecorator decorator = new HTMLDecorator();

        decorator.decorate(list);
        return decorator.outputHtml();

    }

}
