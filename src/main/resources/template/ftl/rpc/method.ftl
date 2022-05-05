# ${interfaceName}
<#if interfaceNotes?? && interfaceNotes != ''>
> ${interfaceNotes}
</#if>
## 方法信息
```
{{qualifiedMethodName}}
```
## 入参说明
<#if jsonExample?? && jsonExample != '' && jsonExample != '{}' >
### 入参示例
```json
${jsonExample}
```
<#else>
> 此接口无任何入参
</#if>

<#assign fieldList = []>
<#list paramLevelMap?keys as level>
    <#assign levelList = paramLevelMap[level]!>
    <#list levelList as levelInfo>
        <#list levelInfo.fieldList as field>
            <#if (field.level > 0)>
                <#assign fieldList += [field]>
            </#if>
        </#list>
    </#list>
</#list>


### 入参字段说明

| **字段** | **类型** | **必填** | **含义** | **其他参考信息** |
| -------- | -------- | -------- | -------- | -------- |
<#list fieldList?sort_by("index") as field>
|${field.levelPrefix} ${field.fieldName}     | **${field.fieldTypeName}**     | ${field.required?string('**是**','否')}  |  ${field.fieldDesc} | ${field.notes}  |
</#list>
</#if>

## 出参
<#if returnJsonExample?? && returnJsonExample != '' && returnJsonExample != '{}' >
### 出参示例
```json
${returnJsonExample}
```
<#else>
> 此接口无任何出参
</#if>

<#assign returnFieldList = []>
<#list returnLevelMap?keys as level>
    <#assign levelList = returnLevelMap[level]!>
    <#list levelList as levelInfo>
        <#list levelInfo.fieldList as field>
            <#if (field.level > 0)>
                <#assign returnFieldList += [field]>
            </#if>
        </#list>
    </#list>
</#list>

<#if returnFieldList?? && (returnFieldList?size > 0)>
### 出参字段说明

| **字段** | **类型**  | **含义** | **其他参考信息** |
| -------- | -------- | -------- | -------- |
<#list returnFieldList?sort_by("index") as return>
|${return.levelPrefix} ${return.fieldName}     | **${return.fieldTypeName}**    |  ${return.fieldDesc} | ${return.notes}  |
</#list>
</#if>


${codeMemo}