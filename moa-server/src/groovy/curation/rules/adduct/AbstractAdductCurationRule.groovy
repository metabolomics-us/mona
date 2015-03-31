package curation.rules.adduct

import curation.AbstractCurationRule
import curation.CurationAction
import curation.CurationObject
import moa.MetaDataValue
import moa.Spectrum
import moa.SupportsMetaData
import moa.server.metadata.MetaDataPersistenceService

/**
 * Created by sajjan on 10/9/14.
 */
abstract class AbstractAdductCurationRule extends AbstractCurationRule {

    MetaDataPersistenceService metaDataPersistenceService

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
        super()
    }

    public AbstractAdductCurationRule(CurationAction successAction, CurationAction failureAction) {
        super(successAction, failureAction)
    }

    /**
     *
     * returns the actual mass which is the adduct, not the exact value!
     * @param mz array of m/z values
     * @param adduct_match mass of adduct to check against list
     * @param toleranceInDalton tolerance of m/z match
     * @return whether the list of m/z values contains
     */
    public double hasMzMatch(def mz, double adductMass, double toleranceInDalton) {

        for (double mass : mz) {
            if (Math.abs(adductMass - mass) < toleranceInDalton) {
                logger.debug("\t=> found ion with difference " + Math.abs(adductMass - mass))
                return mass
            }
        }

        return -1
    }

    /**
     *
     * @param spectrum spectrum object to validate
     * @param adducts map of adducts and corresponding formula
     * @param compoundMass mass of spectrum object
     * @param toleranceInDalton
     * @return
     */
    public Map<String, Double> findAdductMatches(Spectrum spectrum,
                                                 def adducts, double compoundMass, double toleranceInDalton) {
        def identifiedAdducts = [:]

        // Get m/z values
        def mz = spectrum.spectrum.split(' ').collect { ion ->
            Double.parseDouble(ion.split(':')[0])
        }

        adducts.each { adduct, formula ->
            double adductMass = formula(compoundMass)

            logger.debug("Checking adduct " + adduct + " at m = " + adductMass)

            double foundAdduct = hasMzMatch(mz, adductMass, toleranceInDalton)
            if (foundAdduct != -1) {
                logger.debug("\t=> found adduct: ${foundAdduct}")
                identifiedAdducts.put(adduct, foundAdduct)
            }
        }

        return identifiedAdducts
    }

    /**
     * Searches through the metadata of a spectrum or compound object for a specific value
     * @param object spectrum or compound object
     * @param metaDataName name of metadata value to search for
     * @return string value of metadata value if found, otherwise null
     */
    public String findMetaDataValue(SupportsMetaData object, String metaDataName) {
        for (MetaDataValue metaDataValue : object.getMetaData()) {
            logger.debug("checking for meta data value ${metaDataName} against field: ${metaDataValue.name}")

            if (metaDataValue.name.toLowerCase() == metaDataName) {
                logger.debug("\t=> found match " + metaDataValue.value.toString())
                return metaDataValue.value.toString()
            }
        }

        return null
    }

    @Override
    final boolean executeRule(CurationObject toValidate) {
        Spectrum spectrum = toValidate.getObjectAsSpectra()

        if (isValidSpectraForRule(spectrum)) {
            // Get mass and ion mode
            def compoundMass = findMetaDataValue(spectrum.getBiologicalCompound(), "total exact mass");
            def ionMode = findMetaDataValue(spectrum, "ion mode")

            // Check that mass and ion mode were found
            if (compoundMass == null) {
                logger.debug("unable to find mass in biological compound meta data!")
                return false;
            } else {
                compoundMass = Double.parseDouble(compoundMass.toString())
            }

            if (requiresIonMode() && ionMode == null) {
                logger.debug("unable to find ion mode in meta data!")
                return false;
            } else {
                ionMode = ionMode.toString().toLowerCase()
            }

            // Get adduct table
            def adductTable = getAdductTable(ionMode, spectrum)
            adductTable.each { k, v ->
                logger.info("registered adduct for search: ${k}")
            }
            return validateFoundMatches(findAdductMatches(spectrum, adductTable, compoundMass, toleranceInDalton), spectrum)
        } else {
            //if this spectrum doesn't apply the rule is always successful
            return true
        }
    }

    /**
     * returns our adduct table
     * @return
     */
    abstract Map<String, Closure> getAdductTable(String ionMode, Spectrum spectrum)

    /**
     * method to validate the matches
     * @param matches
     * @return
     */
    boolean validateFoundMatches(Map matches, Spectrum spectrum) {

        logger.debug("Found " + matches.size() + " / " + minAdducts + " adducts")

        if (updateSpectraAnnotations) {
            matches.each { String key, Double value ->
                metaDataPersistenceService.generateMetaDataObject(spectrum, [name: key, value: value, category: "annotation", computed: true])
            }
        }
        return (matches.size() >= minAdducts);

    }

    /**
     * do we require the spectra to have the ionmode metadata
     * @return
     */
    abstract boolean requiresIonMode()

    /**
     * a simple check if this spectrum is actually valid for this adduct curation rule
     * @param spectrum
     * @return
     */
    abstract boolean isValidSpectraForRule(Spectrum spectrum)

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }
}
