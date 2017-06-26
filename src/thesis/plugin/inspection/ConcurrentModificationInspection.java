package thesis.plugin.inspection;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thesis.plugin.impl.ExpressionGraph;

import java.util.Arrays;
import java.util.List;

import static thesis.plugin.impl.Constants.MY_OWN_GROUP;

/**
 * Created by Maria on 6/2/2017.
 */
public class ConcurrentModificationInspection extends BaseJavaLocalInspectionTool {

    private static final List<String> SET_LIST_METHODS =
            Arrays.asList("add", "addAll", "clear", "remove", "removeAll", "replaceAll", "removeIf", "set");
    private static final List<String> MAP_METHODS =
            Arrays.asList( "clear", "compute", "computeIfAbsent", "computeIfPresent",
                    "merge", "put", "putAll", "putIfAbsent", "remove");


    private static final String DESC = "No function should change the original container while iterating on it.";

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
        return "Concurrent Modification: No function should change the original container.";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ConcurrentModificationInspection";
    }


    @Nullable
    @Override
    public String getStaticDescription() {
        return super.getStaticDescription();
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                if (shouldBeRegistered(expression, "\\.")) {
                    holder.registerProblem(expression,
                            DESC, ProblemHighlightType.ERROR);
                }
            }


            @Override
            public void visitMethodReferenceExpression(PsiMethodReferenceExpression expression) {
                super.visitReferenceExpression(expression);

                // test::add, this::method etc.
                // I need to get the full expression, and see if the first param is actually a container
                // (It's used in the full expr)

//                if (shouldBeRegistered(expression, "\\.")) {
//                    System.out.println("Should be registered!!!");
//                    holder.registerProblem(expression,
//                            "No function should change the original container while iterating on it!", ProblemHighlightType.ERROR);
//                }
                //TODO
            }
        };
    }


    boolean shouldBeRegistered(PsiMethodCallExpression expression, String regex) {
        ExpressionGraph graph = new ExpressionGraph(expression.getText());
        List<String> nodes = graph.getNodes();

        // we want only expr such as test.add() or do().sth() or test::add do::sth
        if (nodes.size() != 2) {
            return false;
        }

        String[] initialList = expression.getText().split(regex);

        // remove the first one, we really don't care about it for now, but we really want to see if the second one
        // it's in a list
        String objName = initialList[0];
        nodes.remove(objName);
        String method = nodes.get(0);


        if (!SET_LIST_METHODS.contains(method) && !MAP_METHODS.contains(method)) {
            return false;
        }

        // if we are here it means that we have an expresion of interest, maybe one that tries to change the original container
        // in order to see if this is happening, we need to go to the upper level and see if we find a for, a foreach or if this is a part of
        // another method expression

        boolean finalResult = false;

        StringBuilder builder = new StringBuilder(objName).append(".").append("size()");
        if( checkForStatement(expression.getParent(), builder.toString())) {
            return true;
        }

        if( checkForEachStatement(expression.getParent(),  objName)) {
                return true;
        }

        // check for java 8 stream.
        return checkInStream(expression.getParent(), objName);
    }


    private boolean checkForStatement(PsiElement parent, String expression) {
        if(parent instanceof PsiClass) {
            //stop
            return false;
        }

        if(parent instanceof PsiForStatement) {
            // now I should check if one of the conditions is to iterate over the list
            PsiForStatement forStatement = (PsiForStatement) parent;
            return forStatement.getCondition().getText().contains(expression);
        }

        return checkForStatement(parent.getParent(), expression);
    }

    private boolean checkForEachStatement(PsiElement parent, String expression) {
        if(parent instanceof PsiClass) {
            //stop
            return false;
        }

        if(parent instanceof PsiForeachStatement) {
            // now I should check if one of the conditions is to iterate over the list
            PsiForeachStatement foreachStatement = (PsiForeachStatement) parent;
            String iteratedValue = foreachStatement.getIteratedValue().getText();
            return iteratedValue.contains(expression);
        }
         return  checkForEachStatement(parent.getParent(), expression);
    }

    private boolean checkInStream(PsiElement child, String objectThatIsModified) {
        PsiElement parent = child.getParent();
        if(parent instanceof PsiClass) {
            //stop
            return false;
        }

        if(child instanceof PsiMethodCallExpression &&
                ((parent instanceof PsiVariable) ||
                        // case 2 List<T> a; a=v.stream..
                        (parent instanceof PsiAssignmentExpression) ||
                        // case 3 v.stream..
                        (parent instanceof PsiExpressionStatement))) {
            // got the stream, need to see if the first element of the stream is the object.
            PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) child;
            String objUsedWithStream = methodCallExpression.getText().split("\\.")[0];

            return objUsedWithStream.equals(objectThatIsModified);
        }

        return checkInStream(parent, objectThatIsModified);
    }



    @Override
    public boolean isEnabledByDefault() {
        return true;
    }
}
