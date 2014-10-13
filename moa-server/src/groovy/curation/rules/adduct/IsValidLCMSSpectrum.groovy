package curation.rules.adduct

import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.MetaDataValue
import moa.Spectrum
import org.apache.log4j.Logger

/**
 * Created by sajjan on 10/1/14.
 */
class IsValidLCMSSpectrum extends IsValidSpectrum {
    private Logger logger = Logger.getLogger(getClass())


    /**
     * tolerance in Daltons
     */
    double toleranceInDalton = 0.5

    /**
     * minimum number of adducts to match to be considered valid
     */
    int minAdducts = 1


    def IsValidLCMSSpectrum() {
        super(new RemoveTagAction("Suspect InChI"), new AddTagAction("Suspect InChI"))
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    boolean executeRule(CurationObject toValidate) {
        Spectrum spectrum = toValidate.getObjectAsSpectra()

        double compoundMass = -1;
        String ionMode = "";

        // Get mass and ion mode
        for(MetaDataValue metaDataValue : spectrum.getBiologicalCompound().getMetaData()) {
            logger.debug("checking for correct biological compound meta data value field: ${metaDataValue.name}")

            if(metaDataValue.name.toLowerCase() == "total exact mass") {
                compoundMass = Double.parseDouble(metaDataValue.value.toString());
                logger.debug("\t=> found mass "+ compoundMass)
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
        if(compoundMass == -1) {
            logger.debug("unable to find mass in biological compound meta data!")
            return false;
        }

        if(ionMode == "") {
            logger.debug("unable to find ion mode in meta data!")
            return false;
        }


        // Get number of adduct matches
        int n = countAdductMatches(spectrum,
                        ionMode == "positive" ? LCMS_POSITIVE_ADDUCTS : LCMS_NEGATIVE_ADDUCTS,
                        compoundMass, toleranceInDalton)

        logger.debug("Found "+ n +" / "+ minAdducts +" adducts")

        return (n >= minAdducts);
    }
}
