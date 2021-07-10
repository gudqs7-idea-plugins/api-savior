package cn.gudqs7.plugins.idea.annotation;

import cn.gudqs7.plugins.idea.pojo.annotation.*;
import cn.gudqs7.plugins.idea.savior.BaseSavior;
import cn.gudqs7.plugins.idea.util.ActionUtil;
import cn.gudqs7.plugins.idea.util.AnnotationHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author wq
 */
public class PsiMethodAnnotationHolderImpl implements AnnotationHolder {

    private PsiMethod psiMethod;

    public PsiMethodAnnotationHolderImpl(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    @Override
    public PsiAnnotation getAnnotation(String qname) {
        return psiMethod.getAnnotation(qname);
    }

    @Override
    public ApiModelPropertyTag getApiModelPropertyByComment() {
        ApiModelPropertyTag apiModelPropertyTag = new ApiModelPropertyTag();
        for (PsiElement child : psiMethod.getChildren()) {
            if (child instanceof PsiComment) {
                PsiComment psiComment = (PsiComment) child;
                String text = psiComment.getText();
                if (text.startsWith("/**") && text.endsWith("*/")) {
                    String[] lines = text.replaceAll("\r", "").split("\n");
                    for (String line : lines) {
                        if (line.contains("/**") || line.contains("*/")) {
                            continue;
                        }
                        line = line.replaceAll("\\*", "").trim();
                        if (StringUtils.isBlank(line)) {
                            continue;
                        }
                        if (line.contains("@") || line.contains("#")) {
                            if (line.startsWith("@code") || line.startsWith("#code")) {
                                // remove @code itself
                                line = line.substring(5).trim();
                                String[] codeInfoArray = line.split(" ");
                                if (codeInfoArray.length > 1) {
                                    String code = codeInfoArray[0];
                                    String message = codeInfoArray[1];
                                    String reason = "";
                                    if (codeInfoArray.length > 2) {
                                        reason = codeInfoArray[2];
                                    }
                                    ResponseCodeInfo codeInfo = new ResponseCodeInfo(code, message, reason);
                                    apiModelPropertyTag.getResponseCodeInfoList().add(codeInfo);
                                }
                                continue;
                            }
                            if (line.startsWith("@hiddenRequest") || line.startsWith("#hiddenRequest")) {
                                line = line.substring(14).trim();
                                String[] hiddenInfoArray = line.split(",");
                                apiModelPropertyTag.getHiddenRequest().addAll(Arrays.asList(hiddenInfoArray));
                                continue;
                            }
                            if (line.startsWith("@hiddenResponse") || line.startsWith("#hiddenResponse")) {
                                line = line.substring(15).trim();
                                String[] hiddenInfoArray = line.split(",");
                                apiModelPropertyTag.getHiddenResponse().addAll(Arrays.asList(hiddenInfoArray));
                                continue;
                            }

                            String[] tagValArray = line.split(" ");
                            String tag = "";
                            String tagVal = null;
                            if (tagValArray.length > 0) {
                                tag = tagValArray[0].trim();
                            }
                            if (tagValArray.length > 1) {
                                tagVal = line.substring(tag.length()).trim();
                            }
                            switch (tag) {
                                case CommentTag.HIDDEN:
                                case CommentTag.SHARP_HIDDEN:
                                    apiModelPropertyTag.setHidden(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.IMPORTANT:
                                case CommentTag.SHARP_IMPORTANT:
                                    apiModelPropertyTag.setImportant(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.NOTES:
                                case CommentTag.SHARP_NOTES:
                                    String notes = apiModelPropertyTag.getNotes(null);
                                    if (notes != null) {
                                        apiModelPropertyTag.setNotes(notes + "&br;" + tagVal);
                                    } else {
                                        apiModelPropertyTag.setNotes(tagVal);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            String oldValue = apiModelPropertyTag.getValue(null);
                            if (oldValue != null) {
                                apiModelPropertyTag.setValue(oldValue + "&br;" + line);
                            } else {
                                apiModelPropertyTag.setValue(line);
                            }
                        }
                    }
                }
                break;
            }
        }
        dealRequestMapping(apiModelPropertyTag);
        return apiModelPropertyTag;
    }

    @Override
    public ApiModelProperty getApiModelPropertyByAnnotation() {
        ApiModelProperty apiModelProperty = new ApiModelProperty();
        boolean hasOperationAnnotation = hasAnnotatation(QNAME_OF_OPERATION);
        if (hasOperationAnnotation) {
            apiModelProperty.setHidden(getAnnotationValueByOperation("hidden"));
            String value = getAnnotationValueByOperation("value");
            String notes = getAnnotationValueByOperation("notes");
            if (StringUtils.isNotBlank(value)) {
                value = value.replaceAll("\\n", "&br;");
            }
            if (StringUtils.isNotBlank(notes)) {
                notes = notes.replaceAll("\\n", "&br;");
            }
            apiModelProperty.setValue(value);
            apiModelProperty.setNotes(notes);
        }
        if (hasAnnotatation(QNAME_OF_RESPONSES)) {
            // 存在多个 code
            List<PsiAnnotation> psiAnnotationList = getAnnotationValueByQname(QNAME_OF_RESPONSES, "value");
            if (psiAnnotationList != null && psiAnnotationList.size() > 0) {
                for (PsiAnnotation psiAnnotation : psiAnnotationList) {
                    Integer code = BaseSavior.getAnnotationValue(psiAnnotation, "code", null);
                    String message = BaseSavior.getAnnotationValue(psiAnnotation, "message", null);
                    ResponseCodeInfo codeInfo = new ResponseCodeInfo(String.valueOf(code), message, "");
                    apiModelProperty.getResponseCodeInfoList().add(codeInfo);
                }
            }
        } else if (hasAnnotatation(QNAME_OF_RESPONSE)) {
            // 存在单个 code
            Integer code = getAnnotationValueByQname(QNAME_OF_RESPONSE, "code");
            String message = getAnnotationValueByQname(QNAME_OF_RESPONSE, "message");
            ResponseCodeInfo codeInfo = new ResponseCodeInfo(String.valueOf(code), message, "");
            apiModelProperty.getResponseCodeInfoList().add(codeInfo);
        }
        dealRequestMapping(apiModelProperty);
        return apiModelProperty;
    }

    private void dealRequestMapping(ApiModelProperty apiModelProperty) {
        boolean hasMappingAnnotation = hasAnyOneAnnotatation(QNAME_OF_MAPPING, QNAME_OF_GET_MAPPING, QNAME_OF_POST_MAPPING, QNAME_OF_PUT_MAPPING, QNAME_OF_DELETE_MAPPING);
        if (hasMappingAnnotation) {
            if (hasAnnotatation(QNAME_OF_MAPPING)) {
                String path = getAnnotationValueByQname(QNAME_OF_MAPPING, "value");
                if (path == null) {
                    path = getAnnotationValueByQname(QNAME_OF_MAPPING, "path");
                }
                apiModelProperty.setUrl(path);
                String method = getAnnotationValueByQname(QNAME_OF_MAPPING, "method");
                if ("{}".equals(method)) {
                    method = "GET/POST";
                }
                apiModelProperty.setMethod(method);
            }
            dealHttpMethod(apiModelProperty, QNAME_OF_POST_MAPPING, RequestMapping.Method.POST);
            dealHttpMethod(apiModelProperty, QNAME_OF_GET_MAPPING, RequestMapping.Method.GET);
            dealHttpMethod(apiModelProperty, QNAME_OF_PUT_MAPPING, RequestMapping.Method.PUT);
            dealHttpMethod(apiModelProperty, QNAME_OF_DELETE_MAPPING, RequestMapping.Method.DELETE);

            // deal controller RequestMapping
            String controllerUrl = "/";
            PsiAnnotation psiAnnotation = psiMethod.getContainingClass().getAnnotation(QNAME_OF_MAPPING);
            if (psiAnnotation != null) {
                controllerUrl = BaseSavior.getAnnotationValue(psiAnnotation, "value", controllerUrl);
                if (controllerUrl == null) {
                    controllerUrl = BaseSavior.getAnnotationValue(psiAnnotation, "path", controllerUrl);
                }
                if (controllerUrl.startsWith("/")) {
                    controllerUrl = controllerUrl.substring(1);
                }
                if (!controllerUrl.endsWith("/")) {
                    controllerUrl = controllerUrl + "/";
                }
            }
            String hostPrefix = getHostPrefix();
            String nowUrl = apiModelProperty.getUrl("");
            if (nowUrl.startsWith("/")) {
                nowUrl = nowUrl.substring(1);
            }
            apiModelProperty.setUrl(hostPrefix + controllerUrl + nowUrl);
            for (PsiElement child : psiMethod.getChildren()) {
                if (child instanceof PsiParameterList) {
                    PsiParameterList parameterList = (PsiParameterList) child;
                    boolean parameterHasRequestBody = false;
                    if (!parameterList.isEmpty()) {
                        for (PsiParameter parameter : parameterList.getParameters()) {
                            PsiAnnotation annotation = parameter.getAnnotation(QNAME_OF_REQUEST_BODY);
                            if (annotation != null) {
                                parameterHasRequestBody = true;
                                break;
                            }
                        }
                    }
                    if (parameterHasRequestBody) {
                        apiModelProperty.setContentType(RequestMapping.ContentType.APPLICATION_JSON);
                    }
                    break;
                }
            }
        }
    }

    private String getHostPrefix() {
        String hostPrefix = "http://%s:%s/";
        String ip = ActionUtil.getIp();
        String port = "8080";
        String portByConfigFile = getPortByConfigFile();
        if (StringUtils.isNotBlank(portByConfigFile)) {
            port = portByConfigFile;
        }
        return String.format(hostPrefix, ip, port);
    }

    private String getPortByConfigFile() {
        String nameYml = "application.yml";
        String portByYmlFile = getPortByYamlFile(nameYml);
        if (StringUtils.isNotBlank(portByYmlFile)) {
            return portByYmlFile;
        }
        String nameYaml = "application.yaml";
        String portByYamlFile = getPortByYamlFile(nameYaml);
        if (StringUtils.isNotBlank(portByYamlFile)) {
            return portByYamlFile;
        }
        String portByPropertiesFile = getPortByPropertiesFile();
        if (StringUtils.isNotBlank(portByPropertiesFile)) {
            return portByPropertiesFile;
        }
        return null;
    }

    private String getPortByPropertiesFile() {
        Project project = psiMethod.getProject();
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, "application.properties", GlobalSearchScope.projectScope(project));
        if (filesByName != null && filesByName.length > 0) {
            String backPort = null;
            for (PsiFile psiFile : filesByName) {
                String text = psiFile.getText();
                try {
                    Properties properties = new Properties();
                    VirtualFile virtualFile = psiFile.getVirtualFile();
                    properties.load(virtualFile.getInputStream());
                    String port = properties.getProperty("server.port");
                    if (StringUtils.isNotBlank(port)) {
                        PsiFile containingFile = psiMethod.getContainingFile();
                        String path = containingFile.getVirtualFile().getPath();
                        String configFilePath = virtualFile.getPath();
                        String projectBasePath1 = getProjectBasePath(path);
                        String projectBasePath2 = getProjectBasePath(configFilePath);
                        if (projectBasePath1.equals(projectBasePath2)) {
                            return port;
                        }
                        if (backPort == null) {
                            backPort = port;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            return backPort;
        }
        return null;
    }

    private String getPortByYamlFile(String name) {
        Project project = psiMethod.getProject();
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, name, GlobalSearchScope.projectScope(project));
        if (filesByName != null && filesByName.length > 0) {
            String backPort = null;
            for (PsiFile psiFile : filesByName) {
                String text = psiFile.getText();
                try {
                    Yaml yaml = new Yaml();
                    Map<String, Object> map = yaml.load(text);
                    if (map != null && map.size() > 0) {
                        Object serverObj = map.get("server");
                        if (serverObj != null && serverObj instanceof Map) {
                            Map server = (Map) serverObj;
                            Object portObj = server.get("port");
                            if (portObj != null) {
                                String port = portObj.toString();
                                PsiFile containingFile = psiMethod.getContainingFile();
                                String path = containingFile.getVirtualFile().getPath();
                                String configFilePath = psiFile.getVirtualFile().getPath();
                                String projectBasePath1 = getProjectBasePath(path);
                                String projectBasePath2 = getProjectBasePath(configFilePath);
                                if (projectBasePath1.equals(projectBasePath2)) {
                                    return port;
                                }
                                if (backPort == null) {
                                    backPort = port;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            return backPort;
        }
        return null;
    }

    private String getProjectBasePath(String path) {
        int indexOf = path.indexOf("src/");
        if (indexOf != -1) {
            return path.substring(0, path.indexOf("src/"));
        }
        return "";
    }

    private void dealHttpMethod(ApiModelProperty apiModelProperty, String qnameOfXxxMapping, String post) {
        if (hasAnnotatation(qnameOfXxxMapping)) {
            String path = getAnnotationValueByQname(qnameOfXxxMapping, "value");
            if (path == null) {
                path = getAnnotationValueByQname(qnameOfXxxMapping, "path");
            }
            apiModelProperty.setUrl(path);
            apiModelProperty.setMethod(post);
        }
    }

    @Override
    public ApiModelProperty getApiModelProperty() {
        ApiModelProperty apiModelProperty = new ApiModelProperty();
        boolean hasMappingAnnotation = hasAnnotatation(QNAME_OF_OPERATION);
        ApiModelPropertyTag apiModelPropertyByComment = getApiModelPropertyByComment();
        if (hasMappingAnnotation) {
            if (apiModelPropertyByComment.isImportant()) {
                apiModelProperty = apiModelPropertyByComment;
            } else {
                apiModelProperty = getApiModelPropertyByAnnotation();
                // only way to set it
                apiModelProperty.setHiddenRequest(apiModelPropertyByComment.getHiddenRequest());
                apiModelProperty.setHiddenResponse(apiModelPropertyByComment.getHiddenResponse());
            }
        } else {
            apiModelProperty = apiModelPropertyByComment;
        }
        return apiModelProperty;
    }

}
