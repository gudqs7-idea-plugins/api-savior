package cn.gudqs7.plugins.common.base.reader;

import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;

import java.util.Map;

/**
 * @author WQ
 * @date 2022/4/4
 */
public interface IStructureAndCommentReader<B> {

    /**
     * 根据结构数据获取想要的数据
     *
     * @param structureAndCommentInfo 结构信息+注释/注解信息
     * @return 想要的数据
     */
    B read(StructureAndCommentInfo structureAndCommentInfo);

    /**
     * 根据结构数据获取想要的数据
     *
     * @param structureAndCommentInfo 结构信息+注释/注解信息
     * @param data                    指定的初始化数据(可当作参数)
     * @return 想要的数据
     */
    B read(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> data);

}
