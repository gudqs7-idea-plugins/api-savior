<#assign fieldList = []>
<#list levelMap?keys as level>
    <#assign levelList = levelMap[level]!>
    <#list levelList as levelInfo>
        <#list levelInfo.fieldList as field>
            <#if (field.level > 0)>
                <#assign fieldList += [field]>
            </#if>
        </#list>
    </#list>
</#list>

<#if fieldList??>
| **字段** | **类型** | **必填** | **含义** | **其他参考信息** |
| -------- | -------- | -------- | -------- | -------- |
<#list fieldList?sort_by("index") as field>
|${field.levelPrefix} ${field.fieldName}     | **${field.fieldTypeName}**     | ${field.required?string('**是**','否')}  |  ${field.fieldDesc} | ${field.notes}  |
</#list>
</#if>
