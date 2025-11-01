<p><b>An interface document generation tool that generates interface call documents based on javadoc comment (or Swagger annotations), including url, content-type, examples and descriptions of input fields, and examples and descriptions of output fields; also supports exporting to Postman and cURL!</b></p>
<p><b>接口文档生成工具, 根据 Javadoc 注释(或 Swagger 注解)生成接口调用文档, 包含 url, content-type, 入参字段示例及说明, 出参字段示例及说明; 同时支持导出到 Postman 以及 cURL!</b></p>

<a href="https://github.com/gudqs7-idea-plugins/api-savior">GitHub</a> |
<a href="https://github.com/gudqs7-idea-plugins/api-savior/issues">Issues</a> |
<a href="https://github.com/gudqs7-idea-plugins/api-savior/wiki/%E5%85%A5%E9%97%A8%E6%95%99%E7%A8%8B">Get Started</a> |
<a href="https://github.com/gudqs7-idea-plugins/api-savior/blob/master/LICENSE">LICENSE</a>
<br/>

<hr/>

<p>若图片显示不全，请<a href="https://github.com/gudqs7-idea-plugins/api-savior/blob/master/README.md">点此打开</a>到网页上查看</p>

<p><a href="https://github.com/gudqs7-idea-plugins/api-savior/blob/master/README_EN.md">English 🇺🇸</a></p>

<h1>Api Savior 是做什么的？</h1>

<ul>
    <li>是一个 IDEA 插件，仅支持 Java 。</li>
    <li>生成 HTTP 接口文档，格式上支持 markdown、html，目的是取代 Swagger。</li>
    <li>理论上支持所有 Spring MVC 注解开发的 HTTP 接口，同理也支持 <code>Feign</code> 的微服务，另外 <code>Dubbo</code> 这种纯接口写法也是支持的。</li>
    <li>同时附带一些相关小能力，如支持导出到 Postman，支持生成 cURL 命令，支持单个数据传输类生成说明文档表格、json 示例。</li>
    <li>移植并完善了两个插件的功能：<a href="https://github.com/gejun123456/intellij-generateAllSetMethod/pull/66">Github
        GenerateAllSetter Pull#66</a> | <a href="https://github.com/yoke233/genSets">Github genSets</a></li>
</ul>

<h2>与 Swagger 的区别</h2>

<ul>
    <li>随时修改，随时生成，无需启动项目</li>
    <li>支持 java doc 注释</li>
    <li>支持 RPC 接口（即 Dubbo/Feign）</li>
    <li>生成的文档带有入参/出参示例，更直观</li>
    <li>多种载体，不局限于网页或文档；比如目前就支持导出到 Postman，后续可以很轻松的实现导出到 Yapi，亦或是类似的平台；我们的目的是，写一次注释，一辈子管用！</li>
</ul>

<h1>为什么这个项目有用？</h1>

<ol>
    <li>代码总是需要点注释，好记性不如烂笔头；而现在，又多了一个写好注释的好理由！</li>
    <li>每次写接口文档都觉得在做苦力活，尤其是管理后台的业务，动辄就是好几个表的增删改查，也就意味着起码十几个接口，这完全是可避免的！</li>
    <li>这个插件并不是是只能生成接口文档，还可以实现更多的偷懒的方式（只要和接口相关的），还有很多等待着我们去发掘！</li>
</ol>

<h1>我该如何开始？</h1>

<h2>1.安装插件</h2>
<p>快点击 Install 吧!</p>

<h2>2.打开一个 Spring MVC 或 Dubbo 项目</h2>

<p>建议直接打开我专门准备的示例项目：<a href="https://github.com/gudqs7-idea-plugins/api-savior-examples">api-savior-examples</a>
</p>

<p><code>shell
    git clone https://github.com/gudqs7-idea-plugins/api-savior-examples
</code></p>

<h2>3.生成文档</h2>

<p>找到一个 Controller 或 RPC 接口类，<br/>
    如 <code>cn.gudqs.example.docer.restful.user.controller.UserController</code><br/>
    在类名上右键，然后点击
    Generate Api Interface Doc 即可<br/>
    <img alt="img.png" src="https://plugins.jetbrains.com/files/16860/screenshot_20b5aaed-01d9-4b1a-8c79-05b12d5ccf32"/><br/>
    文档如下图<br/>
    <img
            alt="img.png" src="https://plugins.jetbrains.com/files/16860/screenshot_b79abf79-fa5c-4ada-9612-02a3cd111158"/></p>

<h2>4.批量生成文档及更多</h2>

<p>直接在项目上右键（或某个目录/某个类/任意多选亦可），然后点击相应的按钮，如下图<br/>
    <img alt="img.png" src="https://plugins.jetbrains.com/files/16860/screenshot_bc50cd7b-2f0b-462e-bd24-25d2dd131b0c"/></p>

<p>假设我点击了 Batch Generate Api Interface Doc，则我会得到一个文件夹，按模块（可自定义，默认是最后两级报名）分子文件夹的 Markdown 接口文档，如下图<br/>
    <img alt="img.png"
         src="https://plugins.jetbrains.com/files/16860/screenshot_908ee612-4d58-48c7-b44d-845a0cda3dbf"/>
</p>

<h2>5.通过 Search Everywhere 搜索 Api</h2>

<p>双击 <code>Shift</code> 进入 <code>Search Everywhere</code> 后切换到 Api，或使用快捷键 <code>Ctrl + \ </code> 或 <code>Ctrl + Alt +
    N</code> 进入如下图界面。<br/>
    此时您可通过 url 或接口描述来搜索并跳转到该接口。</p>

<blockquote><p>为此我单独开了一个项目 <a href="https://github.com/gudqs7-idea-plugins/search-everywhere-api-idea-plugin">search-everywhere-api-idea-plugin</a>，欢迎来
    Star！ </p></blockquote>

<p><img alt="img.png" src="https://plugins.jetbrains.com/files/16860/screenshot_57e612c2-dd09-4ba1-82e6-d736ee21cffe"/></p>

<h1>如果需要，我可以从哪里获得更多帮助？</h1>

<h2>通过提交 Issue 来获取帮助</h2>

<p><a href="https://github.com/gudqs7-idea-plugins/api-savior/issues">点击访问 Github Issue</a></p>

<blockquote><p>欢迎大家提问，欢迎大家一起完善它！</p></blockquote>

<p><strong>另外，我接入了 IDEA 的错误处理组件，因此当发现插件报错提示时，按照 IDEA 提示，可查看错误信息，并一键上报给我（即自动生成一个 Issue）</strong></p>

<h2>通过查看 Wiki 来获取更多说明</h2>

<p><a href="https://github.com/gudqs7-idea-plugins/api-savior/wiki/Get-Started">点击访问 Wiki</a></p>

<h2>通过查看 demo 示例来了解项目实际使用效果</h2>

<ul>
    <li><a href="https://gudqs7-idea-plugins.github.io/api-savior-examples/">点击查看示例项目的 HTML 格式文档效果</a></li>
    <li><a href="https://github.com/gudqs7-idea-plugins/api-savior-examples">点击访问示例项目 Github</a></li>
</ul>

<h2>贡献指南</h2>

<p><a href="https://github.com/gudqs7-idea-plugins/api-savior/blob/master/CONTRIBUTING_CN.md">贡献指南</a></p>

<h1>致谢名单</h1>

<ul>
    <li><a href="https://github.com/gejun123456/intellij-generateAllSetMethod">Github intellij-generateAllSetMethod</a>
    </li>
    <li><a href="https://github.com/yoke233/genSets">Github genSets</a></li>
    <li><a href="https://github.com/newhoo/RESTKit">Github RESTKit</a></li>
</ul>