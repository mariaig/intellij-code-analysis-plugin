package thesis.plugin.inspection;

import com.intellij.codeInsight.generation.GenerateConstructorHandler;
import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInspection.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static thesis.plugin.impl.Constants.MY_OWN_GROUP;

/**
 * Created by Maria on 6/10/2017.
 */
public class UtilityClassesInspection extends BaseJavaLocalInspectionTool {
    private static final String DESC = "Utility classes shouldn't have public constructors.";
    private final GenerateConstructor generateConstructor = new GenerateConstructor();
    private final MakeConstructorPrivate makeConstructorPrivate = new MakeConstructorPrivate();

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
        return "generate private constructor";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UtilityClassesInspection";
    }

    @Nullable
    @Override
    public String getStaticDescription() {
        return "UtilityClassesInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitClass(PsiClass psiClass) {
                super.visitClass(psiClass);
                if (shouldBeRegistered(psiClass)) {
                    // here it can have only 1 constructor (de default one)
                    List<PsiMethod> constructors = Arrays.asList(psiClass.getConstructors());
                    if (constructors.isEmpty()) {
                        holder.registerProblem(psiClass, DESC, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, generateConstructor);
                    } else {
                        holder.registerProblem(constructors.get(0), DESC, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, makeConstructorPrivate);
                    }
                }

            }
        };
    }

    private boolean shouldBeRegistered(PsiClass psiClass) {

        // this class doesn't have any constructor (or if it already have, shouldn't have any field as parameter)
        List<PsiMethod> baseConstructors = Arrays.asList(psiClass.getConstructors());
        if (baseConstructors.size() > 1) {
            return false;
        }
        // need to check that if I have a constractor:
        // 1. this constructor isn't already private
        // 2. this constructor doesn't have have variables as parameter
        if (baseConstructors.size() == 1
                && (baseConstructors.get(0).getModifierList().hasExplicitModifier(PsiModifier.PRIVATE)
                || baseConstructors.get(0).getParameterList().getParameters().length != 0)) {
            return false;
        }


        // now I need to check that all non private variables methods classes are static

        for (PsiField field : psiClass.getAllFields()) {
            PsiModifierList modifierList = field.getModifierList();
            // doesn't match the case if you have non-private non-static fields
            if (!modifierList.hasExplicitModifier(PsiModifier.PRIVATE) &&
                    !modifierList.hasExplicitModifier(PsiModifier.STATIC)) {
                return false;
            }
        }


        for (PsiMethod method : psiClass.getMethods()) {
            PsiModifierList modifierList = method.getModifierList();
            // doesn't match the case if you have non-private non-static fields
            if (!method.isConstructor() &&
                    !modifierList.hasExplicitModifier(PsiModifier.PRIVATE) &&
                    !modifierList.hasExplicitModifier(PsiModifier.STATIC)) {
                return false;
            }
        }

        // inner classes that aren't static can't instantiate them anyway so that's all
        return true;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }


    private class MakeConstructorPrivate implements LocalQuickFix {
        @Override
        @NotNull
        public String getName() {
            return "Make constructor private";
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "private constructor";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiMethod constructor = (PsiMethod) descriptor.getPsiElement();
            PsiModifierList modifierList = constructor.getModifierList();

            modifierList.setModifierProperty(PsiModifier.PUBLIC, false);
            modifierList.setModifierProperty(PsiModifier.PRIVATE, true);
        }
    }


    private class GenerateConstructor implements LocalQuickFix {

        @Override
        @NotNull
        public String getName() {
            return "Generate private constructor";
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "private constructor";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiClass psiClass = (PsiClass) descriptor.getPsiElement();
            PsiMethod constructor = GenerateConstructorHandler.generateConstructorPrototype(
                    psiClass, null, false, new PsiField[0]);
            PsiModifierList modifierList = constructor.getModifierList();

            modifierList.setModifierProperty(PsiModifier.PUBLIC, false);
            modifierList.setModifierProperty(PsiModifier.PRIVATE, true);

            psiClass.add(constructor);
        }
    }

}