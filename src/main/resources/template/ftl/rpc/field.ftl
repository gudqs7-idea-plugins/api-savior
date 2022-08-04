<#if levelMap?? && (levelMap?size > 0)>
    <#assign cnNumberMap = {"1": "一", "2": "二", "3": "三", "4": "四", "5": "五", "6": "六", "7": "七", "8": "八", "9": "九"}>
    <#assign qNameSet = {}>
    <#list levelMap?keys as level>
        <#if (level?number > 0)>

### 第${cnNumberMap[level]!level}层
        <#assign levelList = levelMap[level]!>
        <#list levelList as levelInfo>
            <#assign qName = levelInfo.clazzQname>
            <#if !(qNameSet[qName])??>
                <#assign qNameSet += {qName: "0"}>
#### ${levelInfo.clazzTypeName}
${((levelInfo.clazzDesc)?? && levelInfo.clazzDesc != '')?string('> '+ levelInfo.clazzDesc, '')}

| **字段** | **类型** | **必填** | **含义** | **其他参考信息** |
| -------- | -------- | -------- | -------- | -------- |
                <#list levelInfo.fieldList?sort_by("index") as field>
| ${field.fieldName} | **${((field.fieldTypeCode)?? && field.originalFieldTypeCode == 2)?string('[' + field.fieldTypeName + '](#' + field.originalFieldTypeName + ')', field.fieldTypeName)}** | ${field.required?string('**是**','否')} |  ${field.fieldDesc} | ${field.notes} |
                </#list>
            </#if>
        </#list>
        </#if>
    </#list>
</#if>
