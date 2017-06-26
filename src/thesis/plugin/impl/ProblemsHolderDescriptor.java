package thesis.plugin.impl;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;

import java.util.Optional;

/**
 * Created by Maria on 5/30/2017.
 */
public class ProblemsHolderDescriptor {
    private PsiElement element;
    private String description;
    private Optional<ProblemHighlightType> highlightType;
    private boolean isSyncMethod;

    private ProblemsHolderDescriptor(PsiElement element, String description, Optional<ProblemHighlightType> highlightType,
                                     boolean isSyncMethod) {
        this.element = element;
        this.description = description;
        this.highlightType = highlightType;
        this.isSyncMethod = isSyncMethod;
    }

    public static ProblemsHolderDescriptor of(PsiElement element, String description, ProblemHighlightType problemHighlightType, boolean isSyncMethod) {
        return new ProblemsHolderDescriptor(element, description, Optional.ofNullable(problemHighlightType), isSyncMethod);
    }


    public static ProblemsHolderDescriptor of(PsiElement element, String description, boolean isSyncMethod) {
        return new ProblemsHolderDescriptor(element, description, null, isSyncMethod);
    }


    public PsiElement getElement() {
        return element;
    }

    public String getDescription() {
        return description;
    }

    public Optional<ProblemHighlightType> getHighlightType() {
        return highlightType;
    }
    public boolean isSyncMethod() {return isSyncMethod;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProblemsHolderDescriptor that = (ProblemsHolderDescriptor) o;

        if (element != null ? !element.equals(that.element) : that.element != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return highlightType != null ? highlightType.equals(that.highlightType) : that.highlightType == null;
    }

    @Override
    public int hashCode() {
        int result = element != null ? element.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (highlightType != null ? highlightType.hashCode() : 0);
        return result;
    }
}
