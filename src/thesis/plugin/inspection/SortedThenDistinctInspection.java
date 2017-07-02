package thesis.plugin.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import thesis.plugin.impl.ExpressionGraph;
import thesis.plugin.impl.InvertedNodesExpressionGraph;

import static thesis.plugin.impl.Constants.PLUGIN_GROUP;

/**
 * Created by Maria on 6/24/2017.
 */
public class SortedThenDistinctInspection extends BaseJavaLocalInspectionTool {
    private static final String SORTED = "sorted";
    private static final String DISTINCT = "distinct";
    private static final String DESC = "Performance issue: the correct order is distinct().sorted(..)";
    private final QuickFix fix = new QuickFix();


    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return PLUGIN_GROUP;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "sorted() then distinct()";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SortedThenDistinctInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitCallExpression(expression);

                if (shouldBeRegistered(expression)) {
                    holder.registerProblem(expression, DESC, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fix);
                }
            }
        };
    }

    private boolean shouldBeRegistered(PsiMethodCallExpression expression) {
        ExpressionGraph graph = new ExpressionGraph(expression.getText());
        PsiElement parent = expression.getParent();
        return graph.containsDirectChain(SORTED, DISTINCT) &&
                // case1: List<T> a = v.stream..
                ((parent instanceof PsiVariable) ||
                        // case 2 List<T> a; a=v.stream..
                        (parent instanceof PsiAssignmentExpression) ||
                        // case 3 v.stream..
                        (parent instanceof PsiExpressionStatement));

    }


    @Override
    public boolean isEnabledByDefault() {
        return true;
    }


    private class QuickFix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "invert sorted() - distinct() order";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // psiElement get text - sth is not right with it - so we really need to get the text from the spectific type
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiElement element = descriptor.getPsiElement();
            PsiMethodCallExpression expression = ((PsiMethodCallExpression) element);

            InvertedNodesExpressionGraph graph = new InvertedNodesExpressionGraph(expression.getText());
            String finalExpression = graph.getFinalExpression(SORTED, DISTINCT);
            expression.replace(factory.createExpressionFromText(finalExpression, element));
        }
    }
}