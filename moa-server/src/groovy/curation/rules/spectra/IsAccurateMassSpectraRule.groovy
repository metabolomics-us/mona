package curation.rules.spectra

import curation.CurationRule
import curation.CurationWorkflow
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:45 PM
 */
class IsAccurateMassSpectraRule extends CurationWorkflow implements CurationRule{

    @Override
    protected boolean failByDefault() {
        return true;
    }

    IsAccurateMassSpectraRule() {
        super(new AddTagAction(ACCURATE),new RemoveTagAction(ACCURATE))
    }

    @Override
    protected boolean abortOnFailure() {
        return true
    }
}
