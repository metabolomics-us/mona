package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import moa.MetaDataValue
import moa.Spectrum
import moa.server.metadata.MetaDataPersistenceService
import util.chemical.AdductBuilder
import static util.MetaDataFieldNames.*

/**
 * computes the mass accuracy of the spectra,
 * based on the exactmass and theoretical mass
 */
class CalculateMassAccuracyRule extends AbstractCurationRule {


    MetaDataPersistenceService metaDataPersistenceService

    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()


        String precursorType = null

        Double exactMass = null
        Double theoreticalMass = null


        spectrum.biologicalCompound.listAvailableValues().each { MetaDataValue value ->
            for (String option : ["total exact mass"]) {
                if (value.getName().toLowerCase().equals(option)) {
                    theoreticalMass = Double.parseDouble(value.getValue().toString())

                    break
                }
            }
        }

        try {
            spectrum.listAvailableValues().each { MetaDataValue value ->

                logger.info("value: ${value.getName()} - ${value.getValue()} - ${value.hidden} - ${value.deleted}")

                if (exactMass == null) {
                    if (value.getName().toLowerCase().equals(EXACT_MASS)) {
                        try {
                            exactMass = Double.parseDouble(value.getValue().toString())
                        } catch (NumberFormatException e){

                            logger.warn(e.getMessage(),e)
                            value.suspect = true
                            value.reasonForSuspicion = "should be a numeric value!"
                            value.save()
                        }
                    }
                }

                if (value.getName().toLowerCase().equals(PRECURSOR_MASS.toLowerCase().trim())) {
                    exactMass = Double.parseDouble(value.getValue().toString())
                }

                if (value.getName().toLowerCase().equals(PRECURSOR_TYPE.toLowerCase().trim())) {
                    precursorType = (value.getValue().toString())
                }
            }
        } catch (NumberFormatException e) {}

        if (theoreticalMass != null) {
            logger.info("theoretical mass of compound: " + theoreticalMass)

            if (exactMass != null) {
                logger.info("observed exact mass: " + exactMass)

                if (precursorType != null) {
                    precursorType = precursorType.toUpperCase()
                    logger.info("using precursor type: ${precursorType}")

                    double computedMass = 0
                    if (AdductBuilder.LCMS_NEGATIVE_ADDUCTS.containsKey(precursorType)) {
                        computedMass = AdductBuilder.LCMS_NEGATIVE_ADDUCTS.get(precursorType)(theoreticalMass)
                        logger.info("computed theoretical mass of adduct: ${computedMass}")
                    } else if (AdductBuilder.LCMS_POSITIVE_ADDUCTS.containsKey(precursorType)) {
                        computedMass = AdductBuilder.LCMS_POSITIVE_ADDUCTS.get(precursorType)(theoreticalMass)
                        logger.info("computed theoretical mass of adduct: ${computedMass}")
                    } else {
                        logger.warn("unknown precursor type: ${precursorType}")
                        return false;
                    }

                    Double massError = exactMass - computedMass;
                    Double accuracy = Math.abs(massError) / exactMass * 1000000
                    logger.info("accuracy mass: ${accuracy}")

                    metaDataPersistenceService.generateMetaDataObject(spectrum, [name: MASS_ACCURACY, value: accuracy, category: "mass spectrometry", unit: "ppm", computed: true])
                    metaDataPersistenceService.generateMetaDataObject(spectrum, [name: MASS_ERROR, value: massError * 1000, category: "mass spectrometry", unit: "mDa", computed: true])
                } else {
                    logger.warn("no precursor found for this spectra")
                    return false;
                }
            } else {
                logger.warn("no exact mass found for this spectra")
                return false;
            }
        } else {
            logger.warn("no theoretical mass found for this spectra")
            return false;
        }

        return false
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "calculates the mass accuracy of the given spectra and adds this as metadata field"
    }
}

