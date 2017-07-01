package thesis.plugin.inspection;

import com.google.caliper.internal.guava.collect.Sets;
import com.google.common.collect.Maps;
import com.intellij.codeInsight.completion.AllClassesGetter;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thesis.plugin.impl.ProblemsHolderDescriptor;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static thesis.plugin.impl.Constants.MY_OWN_GROUP;

/**
 * Created by Maria on 5/30/2017.
 */
public class ParallelStreamInspection extends BaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance(ParallelStreamInspection.class);
    private static final String DESC_SYNC_METH = "This method is used with parallelStream(). " +
            "You might want to use stream() instead, or remove the synchronized modifier.";

    private final LocalQuickFix parallelStreamFix = new ParallelStreamQuickFix();
    private final LocalQuickFix removeSynchronizedFix = new SynchronizedQuickFix();

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
        return "parallel stream with synchronized methods";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ParallelStreamInspection";
    }


    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitClass(PsiClass psiClass) {
                super.visitClass(psiClass);
                Set<ProblemsHolderDescriptor> problems = getClassProblems(psiClass);

                if (problems != null) {
                    problems.forEach(
                            problem -> {
                                LocalQuickFix fix = parallelStreamFix;
                                if (problem.isSyncMethod()) {
                                    fix = removeSynchronizedFix;
                                }
                                holder.registerProblem(problem.getElement(),
                                        problem.getDescription(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fix);
                            }
                    );
                }
            }
        };
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }


    private Set<ProblemsHolderDescriptor> getClassProblems(PsiClass inspectedClass) {

        // if the current class contains synchronized methods, just see if these methods are used in a parallel stream
        Set<ProblemsHolderDescriptor> classProblems = Sets.newHashSet();
        for (PsiMethod method : inspectedClass.getMethods()) {
            if (method.getModifierList().hasExplicitModifier(PsiModifier.SYNCHRONIZED)) {
                // search for the method usages
                classProblems.addAll(getMethodProblems(method));
            }
        }



        // if the current class uses parallel stream, search at project level for sync methods and see if these methods are used
        // with parallel stream in this class
        if(inspectedClass.getText().contains("parallelStream")) {

            InspectionProcessor cacheProcessor = new InspectionProcessor(inspectedClass);

            AllClassesGetter.processJavaClasses(
                    new PlainPrefixMatcher(""),
                    inspectedClass.getProject(),
                    GlobalSearchScope.projectScope(inspectedClass.getProject()),
                    cacheProcessor
            );

            classProblems.addAll(cacheProcessor.getClassProblems());
        }


        return classProblems;
    }


    private Set<ProblemsHolderDescriptor> getMethodProblems(PsiMethod method) {
        Collection<PsiReference> allUsages = MethodReferencesSearch.search(method, true).findAll();
        Set<ProblemsHolderDescriptor> problems = Sets.newHashSet();

        allUsages.stream().filter(
                psiReference -> !(psiReference instanceof PsiImportStaticReferenceElement)
        ).forEach(
                psiReference -> {
                    PsiElement psiElement = psiReference.getElement();
                    PsiElement identifier = searchForParallelStreamIdentifier(psiElement.getParent(), psiElement);

                    if (null != identifier) {
                        ProblemsHolderDescriptor descriptor = ProblemsHolderDescriptor.of(
                                method,
                                DESC_SYNC_METH,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                true
                        );
                        problems.add(descriptor);
                    }

                }
        );
        return problems;
    }


    private static class InspectionProcessor implements Processor<PsiClass> {
        private PsiClass inspectedClass;
        private Set<ProblemsHolderDescriptor> classProblems = Sets.newHashSet();

        public InspectionProcessor(PsiClass inspectedClass) {
            this.inspectedClass = inspectedClass;
        }

        @Override
        public boolean process(PsiClass psiClass) {
            // for the current class, get all the methods that have synchronized as modifier
            List<PsiMethod> synchMethods = Arrays.stream(psiClass.getMethods()).filter(
                    method -> method.getModifierList().hasExplicitModifier(PsiModifier.SYNCHRONIZED)
            ).collect(toList());

            //for these methods, get all usages in order to have the classes where these methods are used
            synchMethods.forEach(
                    method -> {
                        Collection<PsiReference> allUsages = MethodReferencesSearch.search(method, true).findAll();
                        searchUsagesInTheCurrentClass(allUsages, method.getName());
                    }
            );

            return true;
        }

        private void searchUsagesInTheCurrentClass(Collection<PsiReference> allUsages, String methodName) {
            allUsages.stream().filter(
                    psiReference -> !(psiReference instanceof PsiImportStaticReferenceElement)
            ).forEach(
                    psiReference -> {
                        PsiElement psiElement = psiReference.getElement();
                        PsiClass syncMethUsageClass = getClassFromPsiElement(psiElement);
                        PsiElement identifier = searchForParallelStreamIdentifier(psiElement.getParent(), psiElement);

                        if (null != identifier &&
                                inspectedClass.getQualifiedName().equals(syncMethUsageClass.getQualifiedName())) {
                            String description = "Performance issue: " + methodName + " is a synchronized method. " +
                                    "You might consider using stream() instead of parallelStream()";

                            ProblemsHolderDescriptor descriptor = ProblemsHolderDescriptor.of(
                                    identifier, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    false
                            );
                            classProblems.add(descriptor);
                        }
                    }
            );
        }

        public Set<ProblemsHolderDescriptor> getClassProblems() {return classProblems;}
    }

    private static PsiElement searchForParallelStreamIdentifier(PsiElement parent, PsiElement child) {
        if (parent instanceof PsiMethod) {
            // when we get at the parent method it's enough => we didn't find what we were looking for in this method
            return null;
        }
        // a stream methods chain can have only 3 parents: variable, assignment or expr statement

        if (child.getText().contains("parallelStream") &&
                ((parent instanceof PsiVariable) ||
                        // case 2 List<T> a; a=v.stream..
                        (parent instanceof PsiAssignmentExpression) ||
                        // case 3 v.stream..
                        (parent instanceof PsiExpressionStatement))) {
            return child;
        }

        // parent becomes the child
        return searchForParallelStreamIdentifier(parent.getParent(), parent);
    }

    static PsiClass getClassFromPsiElement(PsiElement element) {
        if (element instanceof PsiClass) {
            return (PsiClass) element;
        }

        return getClassFromPsiElement(element.getParent());
    }


    private static class ParallelStreamQuickFix implements LocalQuickFix {
        @NotNull
        public String getName() {
            // The test (see the TestThisPlugin class) uses this string to identify the quick fix action.
            return "use stream()";
        }


        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            try {
                PsiElement element = descriptor.getPsiElement();
                PsiMethodCallExpression expression = ((PsiMethodCallExpression) element);
                PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();


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


    private static class SynchronizedQuickFix implements LocalQuickFix {
        @NotNull
        public String getName() {
            // The test (see the TestThisPlugin class) uses this string to identify the quick fix action.
            return "remove 'syncronized' modifier";
        }


        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            try {
                PsiMethod psiMethod = (PsiMethod) descriptor.getPsiElement();
                PsiModifierList modifierList = psiMethod.getModifierList();

                modifierList.setModifierProperty(PsiModifier.SYNCHRONIZED, false);
            } catch (IncorrectOperationException e) {
                LOG.error(e);
            }
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }
    }

}
