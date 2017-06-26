package thesis.plugin.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * Created by Maria on 6/2/2017.
 */
public class ConcurrentModificationInspectionProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[] {ConcurrentModificationInspection.class};
    }
}
