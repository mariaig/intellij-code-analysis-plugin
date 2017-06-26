package thesis.plugin.inspection;

        import com.intellij.codeInspection.InspectionToolProvider;

/**
 * Created by Maria on 6/10/2017.
 */
public class UtilityClassesInspectionsProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[] {UtilityClassesInspection.class};
    }
}