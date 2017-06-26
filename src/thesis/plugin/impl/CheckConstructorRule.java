package thesis.plugin.impl;


import com.intellij.psi.PsiMethod;

/**
 * Created by Maria on 5/2/2017.
 */
public class CheckConstructorRule {
    public boolean isAllowed(PsiMethod constructor) {
        return constructor.getParameterList().getParametersCount() <= 5;
    }

}
