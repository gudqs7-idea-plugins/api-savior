<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <style type="text/css">

        :root {
            --monospace: "Lucida Console", Consolas, "Courier", monospace;
        }

        #markdown {
            font-family: "Open Sans", "Clear Sans", "Helvetica Neue", Helvetica, Arial, 'Segoe UI Emoji', sans-serif;
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
            font-family: var(--monospace);
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
            font-size: 14px;
            line-height: 22px;
            white-space: pre;
            overflow-x: auto;
            font-family: var(--monospace);
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

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.5.1/styles/base16/solarized-light.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.5.1/highlight.min.js"></script>
    <script>hljs.highlightAll();</script>
</head>

<body>
<article id='markdown'>
  ${markdownDoc}
</article>
</body>
</html>