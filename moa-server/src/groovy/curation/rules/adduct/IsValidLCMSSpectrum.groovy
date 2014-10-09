package curation.rules.adduct

import curation.AbstractCurationRule
import curation.CurrationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.MetaDataValue
import moa.Spectrum
import org.apache.log4j.Logger

/**
 * Created by sajjan on 10/1/14.
 */
class IsValidLCMSSpectrum extends AbstractCurationRule {
    private Logger logger = Logger.getLogger(getClass())

    /**
     * Definitions of positive and negative adducts from
     * http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
     */
    final POSITIVE_ADDUCTS = [
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

    final NEGATIVE_ADDUCTS = [
            "[M-3H]-": {double M -> M / 3.0 - 1.007276},
            "[M-2H]-": {double M -> M / 2.0 - 1.007276},
            "[M-H2O-H]-": {double M -> M- 19.01839},
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

    def TOL = 0.5
    def N_ADDUCTS = 1


    def IsValidLCMSSpectrum() {
        super(new RemoveTagAction("suspect inchi"), new AddTagAction("suspect inchi"))
    }

    @Override
    boolean ruleAppliesToObject(CurrationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    boolean executeRule(CurrationObject toValidate) {
        Spectrum spectrum = toValidate.getObjectAsSpectra()

        double compound_mass = -1;
        String ionMode = "";

        // Get mass and ion mode
        for(MetaDataValue metaDataValue : spectrum.getBiologicalCompound().getMetaData()) {
            logger.debug("checking for correct biological compound meta data value field: ${metaDataValue.name}")

            if(metaDataValue.name.toLowerCase() == "total exact mass") {
                compound_mass = Double.parseDouble(metaDataValue.value.toString());
                logger.debug("\t=> found mass "+ compound_mass)
            }
        }

        for(MetaDataValue metaDataValue : spectrum.getMetaData()) {
            logger.debug("checking for correct meta data value field: ${metaDataValue.name}")

            if (metaDataValue.name.toLowerCase() == "ion mode") {
                ionMode = metaDataValue.value.toString().toLowerCase();
                logger.debug("\t=> found ion mode "+ ionMode)
            }
        }

        // Check that mass and ion mode were found
        if(compound_mass == -1) {
            logger.debug("unable to find mass in biological compound meta data!")
            return false;
        }

        if(ionMode == "") {
            logger.debug("unable to find ion mode in meta data!")
            return false;
        }


        // Get m/z values
        def mz = []

        spectrum.spectrum.split(' ').each() { ion ->
            mz.add(Double.parseDouble(ion.split(':')[0]))
        }


        // Check that at least N adducts exist
        def n = 0


        for(e in (ionMode == "positive" ? POSITIVE_ADDUCTS : NEGATIVE_ADDUCTS)) {
            double adduct_mass = e.value(compound_mass)

            logger.debug("Checking adduct "+ e.key +" at m = "+ adduct_mass)

            boolean result = mz.any { double mass ->
                if(Math.abs(adduct_mass - mass) < TOL) {
                    logger.debug("\t=> found ion with difference "+ Math.abs(adduct_mass - mass))
                    return true
                }
            }

            if(result)
                n++
        }

        logger.debug("Found "+ n +" / "+ N_ADDUCTS +" adducts")

        return (n >= N_ADDUCTS);
    }
}
