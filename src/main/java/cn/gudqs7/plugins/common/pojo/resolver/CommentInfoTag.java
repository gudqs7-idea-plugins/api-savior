package cn.gudqs7.plugins.common.pojo.resolver;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wq
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentInfoTag extends CommentInfo {
    
    /**
     * 是否优先级更高
     */
    private boolean important = false;

}
