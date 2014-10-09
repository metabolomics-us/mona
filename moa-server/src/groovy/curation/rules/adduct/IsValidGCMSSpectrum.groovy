package curation.rules.adduct

import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.MetaDataValue
import moa.Spectrum
import org.apache.log4j.Logger

/**
 * Created by sajjan on 10/9/14.
 */
class IsValidGCMSSpectrum extends IsValidSpectrum {
    private Logger logger = Logger.getLogger(getClass())


    /**
     * tolerance in Daltons
     */
    double toleranceInDalton = 0.5

    /**
     * minimum number of adducts to match to be considered valid
     */
    int minAdducts = 1


    def IsValidGCMSSpectrum() {
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

        // Get mass and ion mode
        for(MetaDataValue metaDataValue : spectrum.getBiologicalCompound().getMetaData()) {
            logger.debug("checking for correct biological compound meta data value field: ${metaDataValue.name}")

            if(metaDataValue.name.toLowerCase() == "total exact mass") {
                compoundMass = Double.parseDouble(metaDataValue.value.toString());
                logger.debug("\t=> found mass "+ compoundMass)
            }
        }

        // Check that mass and ion mode were found
        if(compoundMass == -1) {
            logger.debug("unable to find mass in biological compound meta data!")
            return false;
        }


        // Get number of adduct matches
        int n = countAdductMatches(spectrum, GCMS_ADDUCTS, compoundMass, toleranceInDalton)

        logger.debug("Found "+ n +" / "+ minAdducts +" adducts")

        return (n >= minAdducts);
    }
}
