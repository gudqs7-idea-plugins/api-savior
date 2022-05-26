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

# Docer Savior 是做什么的？

- 是一个 IDEA 插件，仅支持 Java 。
- 生成 HTTP 接口文档，格式上支持 markdown、html，目的是取代 Swagger。
- 理论上支持所有 Spring MVC 注解开发的 HTTP 接口，同理也支持 `Feign` 的微服务，另外 `Dubbo` 这种纯接口写法也是支持的。
- 同时附带一些相关小能力，如支持导出到 Postman，支持生成 cURL 命令，支持单个数据传输类生成说明文档表格、json 示例。
- 移植并完善了两个插件的功能：[Github GenerateAllSetter Pull#66](https://github.com/gejun123456/intellij-generateAllSetMethod/pull/66) | [Github genSets](https://github.com/yoke233/genSets)

## 与 Swagger 的区别

- 随时修改，随时生成，无需启动项目
- 支持 java doc 注释
- 支持 RPC 接口（即 Dubbo/Feign）
- 生成的文档带有入参/出参示例，更直观
- 多种载体，不局限于网页或文档；比如目前就支持导出到 Postman，后续可以很轻松的实现导出到 Yapi，亦或是类似的平台；我们的目的是，写一次注释，一辈子管用！

# 为什么这个项目有用？

1. 代码总是需要点注释，好记性不如烂笔头；而现在，又多了一个写好注释的好理由！
2. 每次写接口文档都觉得在做苦力活，尤其是管理后台的业务，动辄就是好几个表的增删改查，也就意味着起码十几个接口，这完全是可避免的！
3. 这个插件并不是是只能生成接口文档，还可以实现更多的偷懒的方式（只要和接口相关的），还有很多等待着我们去发掘！

# 我该如何开始？

## 1.安装插件
### zip 包安装
从最新的 [Release][latest-release] 页下载 zip 包，然后打开 IDEA，进入 Settings --> Plugins --> 小齿轮 --> Install Plugin from Disk  
![zip](parts/imgs/install-plugin-from-disk.png)

### Marketplace 安装
打开 IDEA，进入 Settings --> Plugins，选中 Marketplace，输入 docer savior 点击 Install  
![Marketplace](parts/imgs/install-from-marketplace.png)

## 2.打开一个 Spring MVC 或 Dubbo 项目
建议直接打开我专门准备的示例项目：[docer-savior-plugin-usage-examples](https://github.com/docer-savior/docer-savior-plugin-usage-examples)    

```shell
git clone https://github.com/docer-savior/docer-savior-plugin-usage-examples
```

## 3.生成文档
找到一个 Controller 或 RPC 接口类，  
如 `cn.gudqs.example.docer.restful.user.controller.UserController`  
在类名上右键，然后点击 Generate Api Interface Doc 即可  
![img.png](parts/imgs/gen-doc-by-class.png)  
文档如下图  
![img.png](parts/imgs/markdown-doc-user.png)


## 4.批量生成文档及更多
直接在项目上右键（或某个目录/某个类/任意多选亦可），然后点击相应的按钮，如下图  
![img.png](parts/imgs/gen-doc-batch.png)
假设我点击了 Batch Generate Api Interface Doc，则我会得到一个文件夹，按模块（可自定义，默认是最后两级报名）分子文件夹的 Markdown 接口文档，如下图    
![img.png](parts/imgs/markdown-doc-batch.png)  


# 如果需要，我可以从哪里获得更多帮助？

## 通过提交 Issue 来获取帮助
 [点击访问 Github Issue](https://github.com/docer-savior/docer-savior-idea-plugin/issues)  
> 欢迎大家提问，欢迎大家一起完善它！

**另外，我接入了 IDEA 的错误处理组件，因此当发现插件报错提示时，按照 IDEA 提示，可查看错误信息，并一键上报给我（即自动生成一个 Issue）**

## 通过查看 Wiki 来获取更多说明

[点击访问 Wiki](https://github.com/docer-savior/docer-savior-idea-plugin/wiki/Get-Started)

## 通过查看 demo 示例来了解项目实际使用效果

- [点击查看示例项目的 HTML 格式文档效果](https://docer-savior.github.io/docer-savior-plugin-usage-examples/)
- [点击访问示例项目 Github](https://github.com/docer-savior/docer-savior-plugin-usage-examples)

# 致谢名单

- [Github intellij-generateAllSetMethod](https://github.com/gejun123456/intellij-generateAllSetMethod)
- [Github genSets](https://github.com/yoke233/genSets)