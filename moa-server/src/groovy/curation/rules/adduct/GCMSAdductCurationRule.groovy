package curation.rules.adduct

import curation.CommonTags
import moa.Spectrum
import moa.Tag

/**
 * Created by sajjan on 10/9/14.
 */
class GCMSAdductCurationRule extends AbstractAdductCurationRule {

    /**
     * Definitions of gcms adducts
     */
    public static final GCMS_ADDUCTS = [
            "[M+TMS]": {double M -> M + 73.1891},
            "[M+2TMS]": {double M -> M + 146.3782},
            "[M+3TMS]": {double M -> M + 219.5673},
    ]

    @Override
    Map<String, Closure> getAdductTable(String ionMode) {
        return GCMS_ADDUCTS
    }

    @Override
    boolean requiresIonMode() {
        return false
    }

    @Override
    boolean isValidSpectraForRule(Spectrum spectrum) {
        for(Tag s : spectrum.getTags()){
            if(s.text == CommonTags.GCMS_SPECTRA){
                return true
            }
        }

        logger.info("no gcms tag found, so wrong object!")

        return false
    }
}
