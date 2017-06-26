package thesis.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.google.common.collect.Maps;
import com.intellij.codeInsight.completion.AllClassesGetter;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import thesis.plugin.impl.CheckConstructorRule;
import thesis.plugin.impl.DependencyTree;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by Maria on 4/30/2017.
 */
public class GenerateDependencyTree extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        // TODO: insert action logic here
        System.out.println("TEST");
        System.out.println(actionEvent.getPlace()); //MainMenu
        Project project = actionEvent.getProject();
        System.out.println(project.getBasePath()); //C:/Users/Maria/IdeaProjects/test-plugin
        ActionManager actionManager = actionEvent.getActionManager();
        System.out.println(actionManager.getComponentName());//ActionManager


        Map<String, Integer> myMap = Maps.newHashMap();
        myMap.put("a", 1);
        myMap.put("B", 2);
        myMap.put("C", 3);

        myMap.entrySet().stream().map(entry -> {
            return entry.getKey() + entry.getValue();
        }).collect(toList());




        // scoare toate .java, chiar si cele care nu-s din proiect
//        Collection<VirtualFile> vFiles = FileBasedIndex.getInstance()
//                .getContainingFiles(
//                        FileTypeIndex.NAME,
//                        JavaFileType.INSTANCE,
//                        GlobalSearchScope.allScope(project));
//        for(VirtualFile vf: vFiles) {
//            System.out.println(vf.getName());
//        }
        System.out.println("------1-----------");
        VirtualFile[] virtualFiles = ProjectRootManager.getInstance(project).getContentRoots();
        for (VirtualFile vf : virtualFiles) {
            System.out.println(vf.getName());
        }
        System.out.println("-------2----------");
        String projectName = project.getName();
        StringBuilder sourceRootsList = new StringBuilder();
        VirtualFile[] vFiles = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for (VirtualFile file : vFiles) {
            sourceRootsList.append(file.getUrl()).append("\n");
            System.out.println(file.getName());
        }

        System.out.println("Source roots for the " + projectName + " plugin:\n" + sourceRootsList);

        System.out.println("--------3---------");
        Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vFiles[0]);
        VirtualFile[] roots = ModuleRootManager.getInstance(module).orderEntries().classes().getRoots();
        VirtualFile[] vFiles2 = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for (VirtualFile file : vFiles2) {
            sourceRootsList.append(file.getUrl()).append("\n");
            System.out.println(file.getName());
            VirtualFile[] children = file.getChildren();
            for (VirtualFile vf : children) {
                System.out.println(vf.getUrl());
            }
        }

        System.out.println("Source roots for the " + projectName + " plugin:\n" + sourceRootsList);
        System.out.println("--------4---------");
        //Pt clasa care este acum deschisa in proiect, scoate toata clasa
//        String fullClassText = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument().getText();
//        System.out.println(fullClassText);
//        System.out.println("--------5---------");
        //full pathul clasei curente
//        Document currentDoc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
//        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
//        String fileName = currentFile.getPath();
//        System.out.println(fileName);


        System.out.println("--------6---------");

        // In stilul asta pot verifica care clasa are mai multi de n parametrii
        // practic pe process ar trebui cumva sa salvez acele clase ale caror constr au mai mult de n params
        Processor<PsiClass> processor = new Processor<PsiClass>() {
            @Override
            public boolean process(PsiClass psiClass) {
                // do your actual work here
                System.out.println("______________________");
                System.out.println(psiClass.getName());
                // qualified name = package name + class name
                System.out.println(psiClass.getQualifiedName());
                Query<PsiClass> inheritors = ClassInheritorsSearch.search(psiClass);
                for (PsiClass inheritor : inheritors.findAll()) {
                    System.out.println("Inheritor: " + inheritor.getQualifiedName());
                }
                System.out.println("______________________");
                return true;
            }
        };

        CheckConstructorRule constructorRule = new CheckConstructorRule();
        MyProcessor processor1 = new MyProcessor(constructorRule);


        AllClassesGetter.processJavaClasses(
                new PlainPrefixMatcher(""),
                project,
                GlobalSearchScope.projectScope(project),
                processor1
        );

        System.out.println(processor1.getClassToInheritorsMap());

        // O alta sugestie pe stack overflow ar fi urmatoarea:
        //To iterate all files in project content, you can use
        // ProjectFileIndex.SERVICE.getInstance(project).iterateContent.
        // Then you can get PSI files from them (PsiManager#findFile),
        // check if they're Java (instanceof PsiJavaFile) and do whatever you like.
        //If you don't need PSI, you can just check the file type
        // (VirtualFile#getFileType == JavaFileType.INSTANCE) and perform
        // the modifications via document (FileDocumentManager#getDocument(file)) or VFS (LoadTextUtil#loadText, VfsUtil#saveText).


        System.out.println("--------7---------");

        DependencyTree dependencyTree = new DependencyTree(processor1.getClassToInheritorsMap());
        System.out.println(dependencyTree.printDOTGraph());

//        DataContext dataContext = actionEvent.getDataContext();
//        final Project project7 = DataKeys.PROJECT.getData(dataContext);
//        final Module module7 = DataKeys.MODULE.getData(dataContext);
//
//
//            final Set<String> packageNameSet = new HashSet<String>();
//
//            AnalysisScope moduleScope = new AnalysisScope(module);
//            moduleScope.accept(new PsiRecursiveElementVisitor() {
//                @Override
//                public void visitFile(final PsiFile file) {
//                    if (file instanceof PsiJavaFile) {
//                        PsiJavaFile psiJavaFile = (PsiJavaFile) file;
//                        System.out.print(file.getName()+"----");
//                        System.out.println(file.getFileType());
////                        PsiClass psiClass = (PsiClass) file;
////                        System.out.println(psiClass.getQualifiedName());
//                        final PsiPackage aPackage =
//                                JavaPsiFacade.getInstance(project).findPackage(psiJavaFile.getPackageName());
//                        if (aPackage != null) {
//                            packageNameSet.add(aPackage.getQualifiedName());
//                        }
//                    }
//                }
//            });
//
//            String allPackageNames = "";
//            for (String packageName : packageNameSet) {
//                allPackageNames = allPackageNames + packageName + "\n";
//            }
//
//        System.out.println("All packages in selected module: "+ allPackageNames);


        //JavaPsiFacade.findClass(qualifiedName, project.getProjectScope).getVirtualFile();


//        PsiFile file =  actionEvent.getData(CommonDataKeys.PSI_FILE);
//        System.out.println(file.toString());
//        ClassInheritorsSearch.search(file.getClass());


//        System.out.println("Rule: " + ConstructorInspection.ACCEPTED_PARAMS);
    }


    public class MyProcessor implements Processor<PsiClass> {

        Map<String, List<String>> classToInheritorsMap = Maps.newHashMap();
        private CheckConstructorRule constructorRule;

        public MyProcessor(CheckConstructorRule constructorRule) {
            this.constructorRule = constructorRule;
        }

        @Override
        public boolean process(PsiClass psiClass) {
            // do your actual work here
            System.out.println("______________________");
            System.out.println(psiClass.getName());
            // qualified name = package name + class name
            System.out.println(psiClass.getQualifiedName());
            Query<PsiClass> inheritors = ClassInheritorsSearch.search(psiClass);
            classToInheritorsMap.put(psiClass.getQualifiedName(),
                    ClassInheritorsSearch.search(psiClass)
                            .findAll()
                            .stream()
                            .map(inheritor -> inheritor.getQualifiedName())
                            .collect(toList()));

            for (PsiClass inheritor : inheritors.findAll()) {
                System.out.println("Inheritor: " + inheritor.getQualifiedName());
            }

            //psi java file from psi class
//            PsiJavaFile javaFile = (PsiJavaFile) psiClass.getContainingFile();

            PsiMethod[] constructors = psiClass.getConstructors();
            for (PsiMethod constructor : psiClass.getConstructors()) {
                System.out.println("Constructor: " + constructor.getName() + " isAllowed: " + constructorRule.isAllowed(constructor)
                        + " constructor decl: " + constructor.getText() + "constructor line: ");
            }


            System.out.println("______________________");
            return true;
        }

        public Map<String, List<String>> getClassToInheritorsMap() {
            return classToInheritorsMap;
        }
    }
}
