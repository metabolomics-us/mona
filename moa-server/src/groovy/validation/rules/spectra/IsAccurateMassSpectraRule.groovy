package validation.rules.spectra

import validation.SpectraValidationRule
import validation.ValidationWorkflow
import validation.actions.AddTagAction
import validation.actions.RemoveTagAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:45 PM
 */
class IsAccurateMassSpectraRule extends ValidationWorkflow implements SpectraValidationRule{

    @Override
    protected boolean failByDefault() {
        return true;
    }

    IsAccurateMassSpectraRule() {
        super(new AddTagAction("accurate"),new RemoveTagAction("accurate"))
    }

    @Override
    protected boolean abortOnFailure() {
        return true
    }
}
