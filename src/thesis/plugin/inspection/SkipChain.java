package thesis.plugin.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thesis.plugin.impl.ExpressionGraph;

import java.util.ArrayList;
import java.util.List;

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
public class SkipChain extends AbstractRedundantChainInspection {
    private static final String SKIP = "skip";
    private static final String DESC = "The 'skip' chain can be reduced to skip(a + b)";
    private final LocalQuickFix fix = new ReduceChainFix(SKIP);

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "redundant chains of 'skip()'";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SkipChain";
    }

    @Override
    String problemDescription() {
        return DESC;
    }

    @Override
    LocalQuickFix getFix() {
        return fix;
    }

    @Override
    boolean shouldBeRegistered(PsiMethodCallExpression expression) {

        ExpressionGraph graph = new ExpressionGraph(expression.getText());
        PsiElement parent = expression.getParent();
        return (graph.containsDirectChain(SKIP, SKIP)) &&
                // case1: List<T> a = v.stream..
                ((parent instanceof PsiVariable) ||
                        // case 2 List<T> a; a=v.stream..
                        (parent instanceof PsiAssignmentExpression) ||
                        // case 3 v.stream..
                        (parent instanceof PsiExpressionStatement));

    }


    @Override
    public String reduce(List<String> arguments) {
        StringBuilder builder = new StringBuilder("(");

        long sum = 0;
        List<String> stringArgs = new ArrayList<>();
        for (String arg : arguments) {
            try {
                long value = Long.valueOf(arg);
                sum += value;
            } catch (NumberFormatException e) {
                // for integers, just add them, for strings, add ", arg"
                stringArgs.add(arg);
            }
        }

        if (stringArgs.isEmpty()) {
            return builder.append(sum).append(")").toString();
        }


        boolean isFirst = true;
        if (sum != 0) {
            builder.append(sum);
            isFirst = false;
        }

        for (String arg : stringArgs) {
            if (isFirst) {
                builder.append(arg);
            } else {
                builder.append(" + ").append(arg);
            }
        }

        builder.append(")");
        return builder.toString();
    }


}

