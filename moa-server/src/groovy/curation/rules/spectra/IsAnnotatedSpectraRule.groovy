package curation.rules.spectra

import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
/**
 * Adds a tag for us, if the spectra has been annotated internally
 *
 * User: wohlgemuth
 * Date: 10/1/14
 * Time: 2:20 PM
 */
class IsAnnotatedSpectraRule extends AbstractMetaDataCentricRule{



    IsAnnotatedSpectraRule() {
        super(new AddTagAction(ANNOTATED_SPECTRA), new RemoveTagAction(ANNOTATED_SPECTRA))
    }


    @Override
    protected boolean acceptMetaDataValue(MetaDataValue val) {
        if (val.category.toLowerCase() == "annotation") {
            return true
        }
        return false
    }


    @Override
    String getDescription() {
        return "this rule determines if a spectra is annotated and should run at the end of the workflow"
    }
}