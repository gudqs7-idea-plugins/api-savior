package cn.gudqs7.plugins.common.enums;

import lombok.Getter;

/**
 * @author wenquan
 * @date 2022/7/6
 */
@Getter
public enum PluginSettingEnum {

    // region Postman setting

    /**
     * 是否自动导出到 Postman 账号下 workspace (通过 Postman Open Api)
     */
    POSTMAN_ENABLE("postman.enable", PluginSettingTypeEnum.BOOL),
    /**
     * Postman Api key, 获取方式见 <a href="https://web.postman.co/settings/me/api-keys">Postman Api keys</a>
     */
    POSTMAN_KEY("postman.key", PluginSettingTypeEnum.STRING),
    /**
     * Postman 文件名称及 workspace 下 Collection 名称
     */
    POSTMAN_NAME("postman.name", PluginSettingTypeEnum.STRING),
    /**
     * 导出到 Postman 账号下 workspace 时, 遇到已存在的 Collection (根据名称) 是否覆盖所有内容~
     */
    POSTMAN_OVERRIDE("postman.override", PluginSettingTypeEnum.BOOL),

    // endregion Postman setting

    // region Amp setting

    AMP_ENABLE("amp.enable", PluginSettingTypeEnum.BOOL),
    AMP_DATA_MODE("amp.data.mode", PluginSettingTypeEnum.STRING),
    AMP_HOST_DAILY("amp.host.daily", PluginSettingTypeEnum.STRING),
    AMP_HOST_PRE("amp.host.pre", PluginSettingTypeEnum.STRING),
    AMP_HOST_PRO("amp.host.pro", PluginSettingTypeEnum.STRING),
    AMP_BACK_APP_NAME("amp.backendService.appName", PluginSettingTypeEnum.STRING),
    AMP_PARAM_SYSTEM("amp.param.system", PluginSettingTypeEnum.STRING),
    AMP_PARAM_REQUEST("amp.param.req", PluginSettingTypeEnum.STRING),

    // endregion Amp setting


    // region OneApi setting
    ONE_API_ENABLE("oneApi.enable", PluginSettingTypeEnum.BOOL),
    ONE_API_NO_TAG("oneApi.noTag", PluginSettingTypeEnum.BOOL),
    ONE_API_NO_MAIN("oneApi.noMain", PluginSettingTypeEnum.BOOL),
    ONE_API_PROJECT_CODE("oneApi.projectCode", PluginSettingTypeEnum.STRING),
    ONE_API_CATALOG_ID("oneApi.catalogId", PluginSettingTypeEnum.STRING),
    ONE_API_API_URL("oneApi.createUrl", PluginSettingTypeEnum.STRING),
    ONE_API_TAG_URL("oneApi.updateTagUrl", PluginSettingTypeEnum.STRING),
    ONE_API_PARAM_REQ("oneApi.param.req", PluginSettingTypeEnum.STRING),
    ONE_API_COOKIE("oneApi.cookie", PluginSettingTypeEnum.STRING),

    // endregion OneApi setting

    // region other setting

    /**
     * 给IP设定一个默认值
     */
    DEFAULT_IP("default.ip", PluginSettingTypeEnum.STRING),
    /**
     * 给URL设置一个前缀, 设置后, IP 失效; 此前缀不包含最后的 /
     */
    DEFAULT_URL_PREFIX("default.url", PluginSettingTypeEnum.STRING),
    /**
     * 若设置为 true, 则生成数据时不生成随机数据(主要用于作者测试)
     */
    DEFAULT_NOT_RANDOM("default.notUsingRandom", PluginSettingTypeEnum.BOOL),
    /**
     * 生成文档所在根目录, 基于IDEA项目根目录下相对路径
     */
    DIR_ROOT("dir.root", PluginSettingTypeEnum.STRING),

    // endregion other setting

    // region prefix

    /**
     * 为原本在文档根目录下子目录们指定新目录, 基于IDEA项目根目录下相对路径; 可能值有 rpc/restful/postman/html..
     */
    PREFIX_DIR("dir.", PluginSettingTypeEnum.STRING),
    /**
     * 尝试指定其他文档模版(整体模版); 可能值为 hsf/restful; 如 template\ftl\restful\method.ftl
     */
    PREFIX_THEME_METHOD("docer.theme.method.", PluginSettingTypeEnum.STRING),
    /**
     * 尝试指定其他文档模版(仅参数模版); 可能值为 hsf/restful; 如 template\ftl\restful\field.ftl
     */
    PREFIX_THEME_FIELD("docer.theme.field.", PluginSettingTypeEnum.STRING),

    // endregion prefix

    ;


    private final String settingKey;
    private final PluginSettingTypeEnum type;

    PluginSettingEnum(String settingKey, PluginSettingTypeEnum type) {
        this.settingKey = settingKey;
        this.type = type;
    }

    @Getter
    public enum PluginSettingTypeEnum {

        /**
         * 设置值类型
         */
        STRING(1),
        BOOL(2),
        INTEGER(3),
        ;

        private final int type;

        PluginSettingTypeEnum(int type) {
            this.type = type;
        }
    }

}
