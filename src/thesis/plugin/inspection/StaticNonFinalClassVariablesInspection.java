package thesis.plugin.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static thesis.plugin.impl.Constants.MY_OWN_GROUP;

/**
 * Created by Maria on 6/10/2017.
 */
public class StaticNonFinalClassVariablesInspection extends BaseJavaLocalInspectionTool {
    private static final String DESC = "Public class variables should be 'final'";


    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return MY_OWN_GROUP;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "static (non-final) class variables";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "StaticNonFinalClassVariablesInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (shouldBeRegistered(field)) {
                    QuickFix fix = new QuickFix(field.getName());
                    holder.registerProblem(field, DESC, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fix);
                }
            }
        };
    }

    private boolean shouldBeRegistered(PsiField field) {
        PsiModifierList modifierList = field.getModifierList();
        return modifierList.hasExplicitModifier(PsiModifier.STATIC) &&
                !modifierList.hasExplicitModifier(PsiModifier.PRIVATE) &&
                !modifierList.hasExplicitModifier(PsiModifier.FINAL);
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }


    private class QuickFix implements LocalQuickFix {

        private final String fieldName;

        private QuickFix(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        @NotNull
        public String getName() {
            return "Make '" + fieldName + "' final";
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "make variable final";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiElement element = descriptor.getPsiElement();
            PsiField field = (PsiField) element;
            PsiModifierList modifierList = field.getModifierList();
            modifierList.setModifierProperty(PsiModifier.FINAL, true);
        }
    }

}