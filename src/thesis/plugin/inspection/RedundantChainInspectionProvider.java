package thesis.plugin.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * Created by Maria on 6/11/2017.
 */
public class RedundantChainInspectionProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[] {SkipChain.class, LimitChain.class};
    }
}

