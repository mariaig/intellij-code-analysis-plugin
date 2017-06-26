package thesis.plugin.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * Created by Maria on 6/11/2017.
 */
public class SortedThenLimitInspectionProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[] {SortedThenLimitInspection.class};
    }
}
