package thesis.plugin.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import thesis.plugin.impl.RedundantChainDetails;
import thesis.plugin.impl.RedundantChainExpressionGraph;

import java.util.List;

import static thesis.plugin.impl.Constants.PLUGIN_GROUP;

/**
 * Created by Maria on 6/10/2017.
 */
public abstract class AbstractRedundantChainInspection extends BaseJavaLocalInspectionTool {

    abstract String reduce(List<String> arguments);

    abstract boolean shouldBeRegistered(PsiMethodCallExpression expression);

    abstract String problemDescription();

    abstract LocalQuickFix getFix();

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return PLUGIN_GROUP;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }


    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitCallExpression(expression);

                if (shouldBeRegistered(expression)) {
                    holder.registerProblem(expression, problemDescription(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, getFix());
                }
            }
        };
    }

    class ReduceChainFix implements LocalQuickFix {
        String nodeName = "";

        public ReduceChainFix(String nodeName) {
            this.nodeName = nodeName;
        }

        @Nls
        @NotNull
        @Override
        public String getName() {
            return "reduce '" + nodeName + "' chain";
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }


        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // psiElement get text - sth is not right with it - so we really need to get the text from the spectific type
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiElement element = descriptor.getPsiElement();
            PsiMethodCallExpression expression = ((PsiMethodCallExpression) element);

            RedundantChainExpressionGraph graph = new RedundantChainExpressionGraph(expression.getText());
            RedundantChainDetails data = graph.getDetails(nodeName);
            String newExpression = reduce(data.getChainArguments());
            String finalExpression = graph.getFinalExpression(nodeName, data, newExpression);
            expression.replace(factory.createExpressionFromText(finalExpression, element));
        }
    }

}

