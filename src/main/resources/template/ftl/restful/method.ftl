# ${interfaceName}
<#if interfaceNotes?? && interfaceNotes != ''>
> ${interfaceNotes}
</#if>

## 请求信息

### 请求地址
```
${url}
```

### 请求方法
```
${method}
```

<#if method?? && method != 'GET' && contentType??>
### 请求体类型
```
${contentType}
```
</#if>

## 入参
<#if jsonExample?? && jsonExample != '' && jsonExample != '{}' >
<#assign jsonExampleType = (contentType?? && contentType=='applicatin/json')?string('RequestBody','Postman Bulk Edit')>
### 入参示例 (${jsonExampleType})
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

<#if fieldList?? && (fieldList?size > 0)>
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

<#if responseCodeInfoList?? && (responseCodeInfoList?size > 0)>
## 更多信息
### Code 更多含义

| Code | 含义 |
| -------- | -------- |
    <#list responseCodeInfoList as responseCode>
| **${responseCode.code}** | ${responseCode.message} |
    </#list>
</#if>
