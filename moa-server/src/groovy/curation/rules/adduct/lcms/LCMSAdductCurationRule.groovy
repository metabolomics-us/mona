package curation.rules.adduct.lcms

import curation.CommonTags
import curation.rules.adduct.AbstractAdductCurationRule
import moa.Spectrum
import moa.Tag
import util.chemical.AdductBuilder

/**
 * Created by sajjan on 10/1/14.
 */
class LCMSAdductCurationRule extends AbstractAdductCurationRule {

    @Override
    Map<String,Closure> getAdductTable(String ionMode,Spectrum spectrum) {

        switch(ionMode){
            case "positive":
                return AdductBuilder.LCMS_POSITIVE_ADDUCTS
                break
            default:
                return AdductBuilder.LCMS_NEGATIVE_ADDUCTS

        }
    }

    @Override
    boolean requiresIonMode() {
        return true
    }

    @Override
    boolean isValidSpectraForRule(Spectrum spectrum) {
        for(Tag s : spectrum.getTags()){
            if(s.text == CommonTags.LCMS_SPECTRA){
                return true
            }
        }

        logger.info("no lcms tag found, so wrong object!")

        return false
    }


    @Override
    String getDescription() {
        return "this rule tries to annotate all LCMS Adducts, found in the spectra"
    }
}
