package thesis.plugin.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Collection;
import java.util.StringTokenizer;

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
public class ParallelStreamVarTypesInspection extends BaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance(ParallelStreamVarTypesInspection.class);
    private final LocalQuickFix parallelStreamFix = new ParallelStreamVarTypesInspection.ParallelStreamFix();

    @NonNls
    public String CHECKED_CLASSES = "java.util.LinkedList; java.util.concurrent.BlockingQueue";


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
        return "parallel stream with non splittable collections";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ParallelStreamVarTypesInspection";
    }


    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitLocalVariable(PsiLocalVariable variable) {
                super.visitLocalVariable(variable);

                PsiNewExpression initializer = null;
                if (variable.getInitializer() instanceof PsiNewExpression) {
                    initializer = (PsiNewExpression) variable.getInitializer();
                }

                if (isCheckedType(variable.getType()) || (initializer != null && isCheckedType(initializer.getType()))) {
                    // keep just the usages that we are interested in
                    // we want to see if this variable is used with parallel stream, so we just need to keep the
                    // references that are actually a PsiExpressionStatement and to contain the word "parallel stream"

                    Collection<PsiReference> allUsages =
                            ReferencesSearch.search(variable)
                                    .findAll();

                    String description = "Performance issue: " +
                            "Variable \"" + variable.getName() + "\" type performs badly for parallel streams." ;
                    allUsages.forEach(
                            var -> {
                                PsiMethodCallExpression expressionStatement = getStatementParent(var.getElement());
                                if (expressionStatement != null && expressionStatement.getText()
                                        .contains("parallelStream()")) {
                                    holder.registerProblem(expressionStatement,
                                            description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, parallelStreamFix);
                                }
                            }
                    );
                }
            }
        };
    }

    private static PsiMethodCallExpression getStatementParent(PsiElement element) {
        if (element instanceof PsiCodeBlock) {
            return null;
        }

        if (element instanceof PsiMethodCallExpression) {
            return (PsiMethodCallExpression) element;
        }

        return getStatementParent(element.getParent());
    }


    private static class ParallelStreamFix implements LocalQuickFix {
        @NotNull
        public String getName() {
            // The test (see the TestThisPlugin class) uses this string to identify the quick fix action.
            return "use stream()";
        }


        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            try {
                PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
                PsiMethodCallExpression expression = (PsiMethodCallExpression) descriptor.getPsiElement();
//                PsiExpression expression = psiExpressionStatement.getExpression();


                String initialExpressionText = expression.getText();
                String finalExpressionText = initialExpressionText.replace("parallelStream", "stream");
                expression.replace(factory.createExpressionFromText(finalExpressionText, expression));
            } catch (IncorrectOperationException e) {
                LOG.error(e);
            }
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }
    }


    private boolean isCheckedType(PsiType type) {
        if (!(type instanceof PsiClassType)) return false;

        StringTokenizer tokenizer = new StringTokenizer(CHECKED_CLASSES, ";");
        while (tokenizer.hasMoreTokens()) {
            String className = tokenizer.nextToken();
            if (type.getCanonicalText().contains(className)) return true;
        }

        return false;
    }


    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField checkedClasses = new JTextField(CHECKED_CLASSES);
        checkedClasses.getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(DocumentEvent event) {
                CHECKED_CLASSES = checkedClasses.getText();
            }
        });

        panel.add(checkedClasses);
        return panel;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }


}
