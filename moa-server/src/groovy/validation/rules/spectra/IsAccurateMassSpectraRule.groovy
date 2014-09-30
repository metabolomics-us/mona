package validation.rules.spectra

import validation.ValidationWorkflow
import validation.actions.AddTagAction
import validation.actions.IgnoreOnFailureAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:45 PM
 */
class IsAccurateMassSpectraRule extends ValidationWorkflow{

    IsAccurateMassSpectraRule() {
        super(new AddTagAction("accurate"),new IgnoreOnFailureAction())

    }

}
