package thesis.plugin.inspection;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import thesis.plugin.impl.ExpressionGraph;

import static thesis.plugin.impl.Constants.PLUGIN_GROUP;

/**
 * Created by Maria on 6/11/2017.
 */
public class SortedThenLimitInspection extends BaseJavaLocalInspectionTool {
    private static final String SORTED = "sorted";
    private static final String LIMIT = "limit";
    //TODO: add proper problemDescription - this is just to know that it works
    private static final String DESC = "Partial sorting: you can use a PriorityQueue to avoid sorting the whole sequence.";


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
        return "sorted() then limit(..)";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SortedThenLimitInspection";
    }


    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitCallExpression(expression);

                if(shouldBeRegistered(expression)) {
                    holder.registerProblem(expression, DESC, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }

    private boolean shouldBeRegistered(PsiMethodCallExpression expression) {

        ExpressionGraph graph = new ExpressionGraph(expression.getText());
        PsiElement parent = expression.getParent();
        return graph.containsDirectChain(SORTED, LIMIT) &&
                // case1: List<T> a = v.stream..
                ( (parent instanceof PsiVariable) ||
                        // case 2 List<T> a; a=v.stream..
                        (parent instanceof PsiAssignmentExpression) ||
                        // case 3 v.stream..
                        (parent instanceof PsiExpressionStatement));

    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }


}
