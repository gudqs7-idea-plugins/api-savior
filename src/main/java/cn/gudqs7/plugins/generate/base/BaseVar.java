package cn.gudqs7.plugins.generate.base;

import com.intellij.psi.PsiType;

/**
 * @author WQ
 * @date 2021/10/6
 */
public class BaseVar {

    private String varName;

    private PsiType varType;

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public PsiType getVarType() {
        return varType;
    }

    public void setVarType(PsiType varType) {
        this.varType = varType;
    }
}
