package thesis.plugin.impl;

import com.intellij.codeInsight.completion.AllClassesGetter;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Processor;
import com.intellij.util.Query;

import static com.intellij.psi.PsiModifier.SYNCHRONIZED;

/**
 * Created by Maria on 5/20/2017.
 */
public class TestAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        // TODO: insert action logic here
        System.out.println("TEST");
        System.out.println(actionEvent.getPlace()); //MainMenu
        Project project = actionEvent.getProject();

        Processor<PsiClass> processor = new Processor<PsiClass>() {
            @Override
            public boolean process(PsiClass psiClass) {
                // do your actual work here

                PsiMethod[] psiMethods = psiClass.getAllMethods();

                for(PsiMethod method : psiMethods) {
                    if(method.getModifierList().hasExplicitModifier(SYNCHRONIZED)) {

                    }
                }

                return true;
            }
        };


        AllClassesGetter.processJavaClasses(
                new PlainPrefixMatcher(""),
                project,
                GlobalSearchScope.projectScope(project),
                processor
        );

    }
}
