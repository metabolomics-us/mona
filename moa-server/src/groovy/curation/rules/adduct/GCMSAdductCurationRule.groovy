package curation.rules.adduct
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
}
