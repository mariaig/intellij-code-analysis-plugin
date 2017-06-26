package thesis.plugin.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * Created by Maria on 6/4/2017.
 */
public class SortedThenFilterInspectionProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[] {SortedThenFilterInspection.class};
    }
}
