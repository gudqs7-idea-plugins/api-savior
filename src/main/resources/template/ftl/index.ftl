<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>文档目录</title>
    <style type="text/css">
        :root {
            --monospace: "Lucida Console", Consolas, "Courier", monospace;
        }

        #catalogue {
            font-family: "Open Sans", "Clear Sans", "Helvetica Neue", Helvetica, Arial, 'Segoe UI Emoji', sans-serif;
            max-width: 1200px;
            margin: 0 auto;
        }

        ul li {
            list-style: none;
            line-height: 30px;
        }

        dd ul {
            padding: 0 0;
        }

        h2 {
            margin: 16px 0;
            padding-bottom: 8px;
            border-bottom: 1px solid #ddd;
        }

    </style>
</head>

<body>

<div id="catalogue">
    <h1>文档目录</h1>

<#list moduleList as module>
    <h2>${module.moduleName}</h2>
    <dl>
    <#list module.fileDirList as fileDir>
        <dt>
            <h3>${fileDir.fileName}</h3>
        </dt>
        <dd>
            <ul>
            <#list fileDir.categoryItemList as categoryItem>
                <li>
                    <a target="_blank" href='${categoryItem.fullFileName}'>${categoryItem.apiName}</a>
                </li>
            </#list>
            </ul>
        </dd>
    </#list>
    </dl>
</#list>

</div>

</body>

</html>