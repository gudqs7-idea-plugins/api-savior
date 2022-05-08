[release-img]: https://img.shields.io/github/release/docer-savior/docer-savior-idea-plugin.svg
[latest-release]: https://github.com/docer-savior/docer-savior-idea-plugin/releases/latest
[plugin-img]: https://img.shields.io/badge/plugin-16860-orange.svg
[plugin]: https://plugins.jetbrains.com/plugin/16860
[jet-img]: https://img.shields.io/badge/plugin-Install%20Plugin-4597ff.svg
[jet]: http://localhost:63342/api/installPlugin?action=install&pluginId=gudqs7.github.io.doc-savior

[![GitHub release][release-img]][latest-release] [![Jetbrains Plugins][plugin-img]][plugin]
[![Version](http://phpstorm.espend.de/badge/16860/version)][plugin]  
[![Downloads](http://phpstorm.espend.de/badge/16860/downloads)][plugin]
[![Install Plugins][jet-img]][jet]

# Generate documents and more based on the controller or interface

[Issues](https://github.com/docer-savior/docer-savior-idea-plugin/issues) |
[Website](https://www.yuque.com/gudqs7/docer/ygtgmz) |
[LICENSE](https://github.com/docer-savior/docer-savior-idea-plugin/blob/master/LICENSE)

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

**使用文档 [https://www.yuque.com/gudqs7/docer/ygtgmz](https://www.yuque.com/gudqs7/docer/ygtgmz)**

#### 效果图

![gen-api](https://github.com/docer-savior/docer-savior-idea-plugin/blob/master/parts/usage/gen-api.gif)  
![export-to-postman](https://github.com/docer-savior/docer-savior-idea-plugin/blob/master/parts/usage/export-to-postman.gif)  
![all-get](https://github.com/docer-savior/docer-savior-idea-plugin/blob/master/parts/usage/allget.gif)  
![all-get-by-intention-action](https://github.com/docer-savior/docer-savior-idea-plugin/blob/master/parts/usage/allget-by-intention-action.gif)  