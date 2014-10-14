package curation.rules.adduct

import curation.CommonTags
import moa.Spectrum
import moa.Tag

/**
 * Created by sajjan on 10/1/14.
 */
class LCMSAdductCurationRule extends AbstractAdductCurationRule {

    /**
     * Definitions of positive mode lcms adducts
     * @link http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
     */
    public static final LCMS_POSITIVE_ADDUCTS = [
            "[M+3H]+": {double M -> M / 3.0 + 1.007276},
            "[M+2H+Na]+": {double M -> M / 3.0 + 8.334590},
            "[M+H+2Na]+": {double M -> M / 3 + 15.7661904},
            "[M+3Na]+": {double M -> M / 3.0 + 22.989218},
            "[M+2H]+": {double M -> M / 2.0 + 1.007276},
            "[M+H+NH4]+": {double M -> M / 2.0 + 9.520550},
            "[M+H+Na]+": {double M -> M / 2.0 + 11.998247},
            "[M+H+K]+": {double M -> M / 2.0 + 19.985217},
            "[M+ACN+2H]+": {double M -> M / 2.0 + 21.520550},
            "[M+2Na]+": {double M -> M / 2.0 + 22.989218},
            "[M+2ACN+2H]+": {double M -> M / 2.0 + 42.033823},
            "[M+3ACN+2H]+": {double M -> M / 2.0 + 62.547097},
            "[M+H]+": {double M -> M + 1.007276},
            "[M+NH4]+": {double M -> M + 18.033823},
            "[M+Na]+": {double M -> M + 22.989218},
            "[M+CH3OH+H]+": {double M -> M + 33.033489},
            "[M+K]+": {double M -> M + 38.963158},
            "[M+ACN+H]+": {double M -> M + 42.033823},
            "[M+2Na-H]+": {double M -> M + 44.971160},
            "[M+IsoProp+H]+": {double M -> M + 61.06534},
            "[M+ACN+Na]+": {double M -> M + 64.015765},
            "[M+2K-H]+": {double M -> M + 76.919040},
            "[M+DMSO+H]+": {double M -> M + 79.02122},
            "[M+2ACN+H]+": {double M -> M + 83.060370},
            "[M+IsoProp+Na+H]+": {double M -> M + 84.05511},
            "[2M+H]+": {double M -> 2 * M + 1.007276},
            "[2M+NH4]+": {double M -> 2 * M + 18.033823},
            "[2M+Na]+": {double M -> 2 * M + 22.989218},
            "[2M+3H2O+2H]+": {double M -> 2 * M + 28.02312},
            "[2M+K]+": {double M -> 2 * M + 38.963158},
            "[2M+ACN+H]+": {double M -> 2 * M + 42.033823},
            "[2M+ACN+Na]+": {double M -> 2 * M + 64.015765}
    ]

    /**
     * Definitions of negative mode lcms adducts
     * @link http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
     */
    public static final LCMS_NEGATIVE_ADDUCTS = [
            "[M-3H]-": {double M -> M / 3.0 - 1.007276},
            "[M-2H]-": {double M -> M / 2.0 - 1.007276},
            "[M-H2O-H]-": {double M -> M - 19.01839},
            "[M-H]-": {double M -> M - 1.007276},
            "[M+Na-2H]-": {double M -> M + 20.974666},
            "[M+Cl]-": {double M -> M + 34.969402},
            "[M+K-2H]-": {double M -> M + 36.948606},
            "[M+FA-H]-": {double M -> M + 44.998201},
            "[M+Hac-H]-": {double M -> M + 59.013851},
            "[M+Br]-": {double M -> M + 78.918885},
            "[M+TFA-H]-": {double M -> M + 112.985586},
            "[2M-H]-": {double M -> 2 * M - 1.007276},
            "[2M+FA-H]-": {double M -> 2 * M + 44.998201},
            "[2M+Hac-H]-": {double M -> 2 * M + 59.013851},
            "[3M-H]-": {double M -> 3 * M - 1.007276},
            "[M+CH3OH+H]-": {double M -> M + 33.033489},
            "[M+K]-": {double M -> M + 38.963158},
            "[M+ACN+H]-": {double M -> M + 42.033823},
            "[M+2Na-H]-": {double M -> M + 44.971160},
            "[M+IsoProp+H]-": {double M -> M + 61.06534},
            "[M+ACN+Na]-": {double M -> M + 64.015765},
            "[M+2K-H]-": {double M -> M + 76.919040},
            "[M+DMSO+H]-": {double M -> M + 79.02122},
            "[M+2ACN+H]-": {double M -> M + 83.060370},
            "[M+IsoProp+Na+H]-": {double M -> M + 84.05511},
            "[2M+H]-": {double M -> 2 * M + 1.007276},
            "[2M+NH4]-": {double M -> 2 * M + 18.033823},
            "[2M+Na]-": {double M -> 2 * M + 22.989218},
            "[2M+3H2O+2H]-": {double M -> 2 * M + 28.02312},
            "[2M+K]-": {double M -> 2 * M + 38.963158},
            "[2M+ACN+H]-": {double M -> 2 * M + 42.033823},
            "[2M+ACN+Na]-": {double M -> 2 * M + 64.015765}
    ]

    @Override
    Map<String,Closure> getAdductTable(String ionMode) {

        switch(ionMode){
            case "positive":
                return LCMS_POSITIVE_ADDUCTS
                break
            default:
                return LCMS_NEGATIVE_ADDUCTS

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
}
