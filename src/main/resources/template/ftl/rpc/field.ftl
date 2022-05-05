<#if levelMap?? && (levelMap?size > 0)>
    <#assign cnNumberMap = {"1": "一", "22": "二", "3": "三", "4": "四", "5": "五", "6": "六", "7": "七", "8": "八", "9": "九"}>
    <#list levelMap?keys as level>
        <#if (level?number > 0)>
### 第${cnNumberMap[level]!level}层

        <#assign levelList = levelMap[level]!>
        <#list levelList as levelInfo>
            <#assign typeStr = levelInfo.clazzTypeName>
            <#if (levelInfo.level > 2 && levelInfo.parentClazzTypeName??)>
                <#assign typeStr = levelInfo.parentClazzTypeName + '=>' + levelInfo.clazzTypeName>
            </#if>
#### ${typeStr} ${((levelInfo.clazzDesc)?? && levelInfo.clazzDesc != '')?string('('+ levelInfo.clazzDesc +')', '')}
| **字段** | **类型** | **必填** | **含义** | **其他参考信息** |
| -------- | -------- | -------- | -------- | -------- |
            <#list levelInfo.fieldList?sort_by("index") as field>
| ${field.fieldName} | **${field.fieldTypeName}** | ${field.required?string('**是**','否')} |  ${field.fieldDesc} | ${field.notes} |
            </#list>
        </#list>
        </#if>
    </#list>

</#if>
