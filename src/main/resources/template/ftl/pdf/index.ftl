<html lang="zh">
<head>
    <meta charset="UTF-8"/>
    <title>接口文档</title>
    <style>
		html {
			font-size: 80%;
            font-family: "Microsoft YaHei";
        }

        h1 {
            margin: 20px 0;
            text-align: center;
        }

        /* Markdown
        -------------------- */


        :root {
            --monospace: "Lucida Console", Consolas, "Courier", monospace;
        }

        #markdown {
            font-family: "Microsoft YaHei";
            max-width: 1200px;
            margin: 0 auto;
        }

        /* Header
        -------------------- */

        #markdown h1, #markdown h2, #markdown h3 {
            cursor: default;
        }

        #markdown h1 {
            margin: 20px 0;
            text-align: center;
        }

        #markdown h2 {
            margin: 16px 0;
            padding-bottom: 8px;
            border-bottom: 1px solid #ddd;
        }

        #markdown h3 {
            position: relative;
            margin: 10px 0;
        }


        #markdown h4 {
            margin: 10px 0;
        }

        /* Text Formating
        -------------------- */

        #markdown p {
            margin: 12px 0;
        }

        #markdown code {
            margin: 0 2px;
            padding: 3px 5px;
            border-radius: 2px;
            color: rgb(236, 90, 22);
            background-color: #eee;
            font-family: "Microsoft YaHei";
        }

        /* Horizontal Rule
        -------------------- */

        #markdown hr {
            height: 2px;
            margin: 16px 0;
            border: none;
            background-color: #ccc;
        }

        /* Cites
        -------------------- */

        #markdown blockquote {
            border-left: 4px solid #0eaeec;
            padding: 10px 15px;
            color: #666;
            background-color: #dbf3fd;
        }

        /* Table
        -------------------- */

        #markdown table {
            border-spacing: 0;
            border-collapse: collapse;
        }

        #markdown table th, #markdown table td {
            padding: 6px 12px;
            border: 1px solid #dfe2e5;
        }

        #markdown table tr {
            background-color: #fff;
        }

        #markdown table thead tr, #markdown table tbody tr:nth-child(even) {
            background-color: #f8f8f8;
        }

        /* Code Block
        -------------------- */

        #markdown pre code {
            display: block;
            padding: 16px 20px;
            border: 1px solid #ddd;
            border-radius: 4px;
            background-color: #f8f8f8;
            color: #525252;
            line-height: 22px;
            white-space: pre;
            overflow-x: auto;
            font-family: "Microsoft YaHei";
        }

        /* List
        -------------------- */

        #markdown ul {
            padding-left: 20px;
            list-style-type: disc;
        }

        #markdown ul li {
            margin: 8px 0;
        }

        #markdown ul ul {
            list-style-type: circle;
        }

        #markdown ul ul ul {
            list-style-type: square;
        }

        /* Link
        -------------------- */

        #markdown a {
            color: #0eaeec;
            font-weight: bold;
        }

        /* Image
        -------------------- */

        #markdown img {
            display: block;
            max-width: 100%;
            height: auto;
            margin: 16px auto;
        }

    </style>

    <bookmarks>
    <#list moduleList as module>
        <bookmark name="${module.moduleName}" href="#${module.moduleName}">
        <#list module.fileDirList as fileDir>
            <bookmark name="${fileDir.fileName}" href="#${module.moduleName}-${fileDir.fileName}">
            <#list fileDir.categoryItemList as categoryItem>
                <bookmark name="${categoryItem.apiName}" href="#${categoryItem.fullFileName}"/>
            </#list>
            </bookmark>
        </#list>
        </bookmark>
    </#list>
    </bookmarks>
</head>

<body>
<#list moduleList as module>
    <h1 id="${module.moduleName}">${module.moduleName}</h1>
    <#list module.fileDirList as fileDir>
        <h1 id="${module.moduleName}-${fileDir.fileName}">${fileDir.fileName}</h1>
        <article id='markdown'>
            ${fileDir.markdownHtml}
        </article>
    </#list>
</#list>

</body>

</html>