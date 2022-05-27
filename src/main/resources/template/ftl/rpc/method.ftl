# ${interfaceName}
<#if interfaceNotes?? && interfaceNotes != ''>
> ${interfaceNotes}
</#if>
## 方法信息
```
${qualifiedMethodName}
```
## 入参说明
<#if jsonExample?? && jsonExample != '' && jsonExample != '{}' >
### 示例
```json
${jsonExample}
```
<#else>
> 此接口无任何入参
</#if>

<#assign cnNumberMap = {"1": "一", "2": "二", "3": "三", "4": "四", "5": "五", "6": "六", "7": "七", "8": "八", "9": "九"}>
<#if paramLevelMap?? && (paramLevelMap?size > 0)>
    <#list paramLevelMap?keys as level>
        <#if (level?number > 0)>

### 第${cnNumberMap[level]!level}层
            <#assign levelList = paramLevelMap[level]!>
            <#list levelList as levelInfo>
#### ${levelInfo.clazzTypeName}
${((levelInfo.clazzDesc)?? && levelInfo.clazzDesc != '')?string('> '+ levelInfo.clazzDesc, '')}

| **字段** | **类型** | **必填** | **含义** | **其他参考信息** |
| -------- | -------- | -------- | -------- | -------- |
                <#list levelInfo.fieldList?sort_by("index") as field>
| ${field.fieldName} | **${((field.fieldTypeCode)?? && field.originalFieldTypeCode == 2)?string('[' + field.fieldTypeName + '](#' + field.originalFieldTypeName + ')', field.fieldTypeName)}** | ${field.required?string('**是**','否')} |  ${field.fieldDesc} | ${field.notes} |
                </#list>
            </#list>
        </#if>
    </#list>
</#if>

## 出参说明
<#if returnJsonExample?? && returnJsonExample != '' && returnJsonExample != '{}' >
### 示例
```json
${returnJsonExample}
```
<#else>
> 此接口无任何出参
</#if>

<#if returnLevelMap?? && (returnLevelMap?size > 0)>
    <#list returnLevelMap?keys as level>
        <#if (level?number > 0)>

### 第${cnNumberMap[level]!level}层
            <#assign levelList = returnLevelMap[level]!>
            <#list levelList as levelInfo>
#### ${levelInfo.clazzTypeName}
${((levelInfo.clazzDesc)?? && levelInfo.clazzDesc != '')?string('> '+ levelInfo.clazzDesc, '')}

| **字段** | **类型** | **含义** | **其他参考信息** |
| -------- | -------- | -------- | -------- |
                <#list levelInfo.fieldList?sort_by("index") as field>
| ${field.fieldName} | **${((field.fieldTypeCode)?? && field.originalFieldTypeCode == 2)?string('[' + field.fieldTypeName + '](#' + field.originalFieldTypeName + ')', field.fieldTypeName)}** |  ${field.fieldDesc} | ${field.notes} |
                </#list>
            </#list>
        </#if>
    </#list>
</#if>

<#if responseCodeInfoList?? && (responseCodeInfoList?size > 0)>
## 更多信息
### Code 更多含义

| Code | 含义 |
| -------- | -------- |
    <#list responseCodeInfoList as responseCode>
| **${responseCode.code}** | ${responseCode.message} |
    </#list>
</#if>