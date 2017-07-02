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

