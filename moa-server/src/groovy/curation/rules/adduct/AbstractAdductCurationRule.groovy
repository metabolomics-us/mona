package curation.rules.adduct
import curation.AbstractCurationRule
import curation.CurationAction
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.MetaDataValue
import moa.Spectrum
import org.apache.log4j.Logger

/**
 * Created by sajjan on 10/9/14.
 */
abstract class AbstractAdductCurationRule extends AbstractCurationRule {

    Logger logger = Logger.getLogger(getClass())

    /**
     * do we want to also add spectra annotation metadata
     */
    boolean updateSpectraAnnotations = true

    /**
     * tolerance in Daltons
     */
    double toleranceInDalton = 0.5

    /**
     * minimum number of adducts to match to be considered valid
     */
    int minAdducts = 1

    public AbstractAdductCurationRule() {
        super(new RemoveTagAction("Suspect InChI"), new AddTagAction("Suspect InChI"))
    }

    public AbstractAdductCurationRule(CurationAction successAction, CurationAction failureAction) {
        super(successAction, failureAction)
    }


    /**
     *
     * @param mz array of m/z values
     * @param adduct_match mass of adduct to check against list
     * @param toleranceInDalton tolerance of m/z match
     * @return whether the list of m/z values contains
     */
    public boolean hasMzMatch(def mz, double adductMass, double toleranceInDalton) {
        return mz.any { double mass ->
            if(Math.abs(adductMass - mass) < toleranceInDalton) {
                logger.debug("\t=> found ion with difference "+ Math.abs(adductMass - mass))
                return true
            }
        }
    }

    /**
     *
     * @param spectrum spectrum object to validate
     * @param adducts map of adducts and corresponding formula
     * @param compoundMass mass of spectrum object
     * @param toleranceInDalton
     * @return
     */
    public Map<String,Double> findAdductMatches(Spectrum spectrum, def adducts, double compoundMass, double toleranceInDalton) {
        def identifiedAdducts = [:]

        // Get m/z values
        def mz = spectrum.spectrum.split(' ').collect { ion ->
            Double.parseDouble(ion.split(':')[0])
        }

        adducts.each { adduct, formula ->
            double adductMass = formula(compoundMass)

            logger.debug("Checking adduct "+ adduct +" at m = "+ adductMass)

            if(hasMzMatch(mz, adductMass, toleranceInDalton)){
                identifiedAdducts.put(adduct,adductMass)
            }
        }

        return identifiedAdducts
    }


    @Override
    final boolean executeRule(CurationObject toValidate) {
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

        if(requiresIonMode()) {
            if (ionMode == "") {
                logger.debug("unable to find ion mode in meta data!")
                return false;
            }
        }

        return validateFoundMatches(findAdductMatches(spectrum,getAdductTable(ionMode),compoundMass,toleranceInDalton))
    }


    /**
     * returns our adduct table
     * @return
     */
    abstract Map<String,Closure> getAdductTable(String ionMode)

    /**
     * method to validate the matches
     * @param matches
     * @return
     */
    boolean validateFoundMatches(Map matches){

        logger.debug("Found "+ matches.size() +" / "+ minAdducts +" adducts")

        return (matches.size() >= minAdducts);

    }

    /**
     * do we require the spectra to have the ionmode metadata
     * @return
     */
    abstract boolean requiresIonMode()

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

}
