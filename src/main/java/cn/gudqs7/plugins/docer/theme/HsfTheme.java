package cn.gudqs7.plugins.docer.theme;

import cn.gudqs7.plugins.docer.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.docer.savior.BaseSavior;
import cn.gudqs7.plugins.docer.util.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;

import java.util.Map;

/**
 * @author wq
 */
public class HsfTheme implements Theme {

    private HsfTheme() {
    }

    private static HsfTheme instance;

    public static HsfTheme getInstance() {
        if (instance == null) {
            synchronized (HsfTheme.class) {
                if (instance == null) {
                    instance = new HsfTheme();
                }
            }
        }
        return instance;
    }

    @Override
    public String getMethodPath() {
        return "template/hsf/Method.txt";
    }

    @Override
    public String getParamContentPath(boolean returnParam) {
        if (returnParam) {
            return "template/hsf/ReturnParamContent.txt";
        }
        return "template/hsf/ParamContent.txt";
    }

    @Override
    public String getParamTitlePath(boolean returnParam) {
        if (returnParam) {
            return "template/hsf/ReturnParamTitle.txt";
        }
        return "template/hsf/ParamTitle.txt";
    }

    @Override
    public void handleParameterList(PsiParameter parameter, AnnotationHolder annotationHolder, Map<String, Object> map) {
        PsiElement parent = parameter.getParent();
        if (parent instanceof PsiParameterList) {
            PsiParameterList psiParameterList = (PsiParameterList) parent;
            if (psiParameterList.getParametersCount()==1) {
                ApiModelProperty apiModelProperty = annotationHolder.getApiModelProperty();
                String fieldName = parameter.getName();
                fieldName = apiModelProperty.getName(fieldName);

                PsiType psiFieldType = parameter.getType();
                String typeName = psiFieldType.getPresentableText();
                if (!BaseSavior.isJavaBaseType(typeName)) {
                    Object obj = map.remove(fieldName);
                    if (obj instanceof Map) {
                        Map paramMap = (Map) obj;
                        if (paramMap.size() > 0) {
                            map.putAll(paramMap);
                            return;
                        }
                    }
                    map.put(fieldName, obj);
                }
            }
        }
    }
}
