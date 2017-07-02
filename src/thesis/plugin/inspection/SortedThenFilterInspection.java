package thesis.plugin.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import thesis.plugin.impl.ExpressionGraph;
import thesis.plugin.impl.InvertedNodesExpressionGraph;

import static thesis.plugin.impl.Constants.PLUGIN_GROUP;

/**
 * Copyright Maria Igescu
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SortedThenFilterInspection extends BaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance(ParallelStreamInspection.class);
    private static final String SORTED = "sorted";
    private static final String FILTER = "filter";
    private static final String DESC = "Performance issue: the correct order is filter(..).sort(..)";
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
        return "sorted() then filter()";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SortedThenFilterInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitCallExpression(expression);

                if(shouldBeRegistered(expression)) {
                    holder.registerProblem(expression, DESC, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fix);
                }
            }
        };
    }

    private boolean shouldBeRegistered(PsiMethodCallExpression expression) {
        ExpressionGraph graph = new ExpressionGraph(expression.getText());
        PsiElement parent = expression.getParent();
        return graph.containsDirectChain(SORTED, FILTER) &&
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


    private class QuickFix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "invert sorted() - filter() order";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // psiElement get text - sth is not right with it - so we really need to get the text from the spectific type
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiElement element =  descriptor.getPsiElement();
            PsiMethodCallExpression expression = ((PsiMethodCallExpression) element);

            InvertedNodesExpressionGraph graph = new InvertedNodesExpressionGraph(expression.getText());
            String finalExpression = graph.getFinalExpression(SORTED, FILTER);
            expression.replace(factory.createExpressionFromText(finalExpression, element));
        }
    }

}
