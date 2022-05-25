[release-img]: https://img.shields.io/github/release/docer-savior/docer-savior-idea-plugin.svg
[latest-release]: https://github.com/docer-savior/docer-savior-idea-plugin/releases/latest
[plugin-img]: https://img.shields.io/badge/plugin-16860-orange.svg
[plugin]: https://plugins.jetbrains.com/plugin/16860
[jet-img]: https://img.shields.io/badge/plugin-Install%20Plugin-4597ff.svg
[jet]: http://localhost:63342/api/installPlugin?action=install&pluginId=gudqs7.github.io.doc-savior

[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)
[![license](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![GitHub release][release-img]][latest-release] [![Jetbrains Plugins][plugin-img]][plugin]
[![Version](http://phpstorm.espend.de/badge/16860/version)][plugin]  
[![Downloads](http://phpstorm.espend.de/badge/16860/downloads)][plugin]
[![Install Plugins][jet-img]][jet]  

---
[中文 🇨🇳](./README_CN.md)  

# Docer Savior 是什么？

- 是一个 IDEA 插件，仅支持 Java 。
- 生成 HTTP 接口文档，格式上支持 markdown、html，目的是取代 Swagger。
- 理论上支持所有 Spring MVC 注解开发的 HTTP 接口，同理也支持 `Feign` 的微服务，另外 `Dobbo` 这种纯接口写法也是支持的。
- 同时附带一些相关小能力，如支持导出到 Postman，支持生成 cURL 命令，支持单个数据传输类生成说明文档表格、json 示例。


# 为什么开发这个项目？

# 我该如何开始？

# 如果需要，我改从哪里获取更多帮助？



### English introduction

    This plugin generates a markdown format interface document based on Swagger annotations with one click, and pastes it directly into any markdown editor, and then you can share it with others without being restricted by Swagger having to start the project!

#### Usage

    Right-click when editing the code, we provide three entrances: on the Java interface, on the Java method, on the ordinary class,

    However, it should be noted that on ordinary classes, only parameter description documents in Markdown format will be generated based on the parameters.

    Right-click on the method to generate a complete interface document in Markdown format according to this method.

    Finally, on the Java interface, a complete interface document in Markdown format is generated according to all methods!

### 中文描述

#### 支持功能如下

* 支持 controller 和普通接口
* 支持导出成 markdown 文档, 或HTML格式文档
* 完美支持Postman, 导出即可立即运行
* 支持单个请求/响应Pojo 生成文档及示例
* 支持导出成cURL, 一键粘贴即可运行(自动获取ip及端口, 仅SpringBoot)

#### 搬迁功能如下

* GenerateAllSetter(改进后添加生成 getter及支持 postfix: xxx.allset) [Github](https://github.com/gejun123456/intellij-generateAllSetMethod)
* RestfulToolkit 没啥改进, 仅保留根据URL跳转代码的搜索框

#### 其他

- **使用文档 [https://www.yuque.com/gudqs7/docer/ygtgmz](https://www.yuque.com/gudqs7/docer/ygtgmz)**
- **文档示例 [https://github.com/docer-savior/docer-savior-plugin-usage-examples](https://github.com/docer-savior/docer-savior-plugin-usage-examples)**

##### 文档示例 - HTML 文档

> 相关文件位于 docs 下, 已使用 github pages 部署, 可通过 [文档首页](https://docer-savior.github.io/docer-savior-plugin-usage-examples/) 直接访问

##### 文档示例 - Markdown 文档

- [用户接口.md](https://github.com/docer-savior/docer-savior-plugin-usage-examples/blob/master/doc-example/restful/%E7%94%A8%E6%88%B7%E6%A8%A1%E5%9D%97/%E7%94%A8%E6%88%B7%E6%8E%A5%E5%8F%A3.md)
- [用户VIP接口.md](https://github.com/docer-savior/docer-savior-plugin-usage-examples/blob/master/doc-example/restful/%E7%94%A8%E6%88%B7%E6%A8%A1%E5%9D%97/%E7%94%A8%E6%88%B7VIP%E6%8E%A5%E5%8F%A3.md)
- [下单接口.md](https://github.com/docer-savior/docer-savior-plugin-usage-examples/blob/master/doc-example/restful/%E8%AE%A2%E5%8D%95%E6%A8%A1%E5%9D%97/%E4%B8%8B%E5%8D%95%E6%8E%A5%E5%8F%A3.md)
- [订单接口.md](https://github.com/docer-savior/docer-savior-plugin-usage-examples/blob/master/doc-example/restful/%E8%AE%A2%E5%8D%95%E6%A8%A1%E5%9D%97/%E8%AE%A2%E5%8D%95%E6%8E%A5%E5%8F%A3.md)


#### 效果图

![gen-api](https://github.com/docer-savior/docer-savior-idea-plugin/raw/master/parts/usage/gen-api.gif)  
![export-to-postman](https://github.com/docer-savior/docer-savior-idea-plugin/raw/master/parts/usage/export-to-postman.gif)  
![all-get](https://github.com/docer-savior/docer-savior-idea-plugin/raw/master/parts/usage/allget.gif)  
![all-get-by-intention-action](https://github.com/docer-savior/docer-savior-idea-plugin/raw/master/parts/usage/allget-by-intention-action.gif)