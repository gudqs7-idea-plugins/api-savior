package cn.gudqs7.plugins.search.pojo;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * RequestMapping 注解信息类
 *
 * @author wq
 * @date 2022/6/10
 */
@Data
public class RequestMappingAnnotationInfo {

    private List<String> value;
    private List<String> path;
    private List<String> params;
    private List<String> method;

    public List<String> getValueOrPath() {
        if (CollectionUtils.isNotEmpty(this.value)) {
            return this.value;
        }
        if (CollectionUtils.isNotEmpty(this.path)) {
            return this.path;
        }
        return List.of("/");
    }

}
