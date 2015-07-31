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


        String precursorType = ""

        Double exactMass = null
        Double theoreticalMass = null


        spectrum.chemicalCompound.metaData.each { MetaDataValue value ->

            for (String option : ["total exact mass"]) {
                if (value.getName().toLowerCase().equals(option)) {
                    theoreticalMass = Double.parseDouble(value.getValue().toString())

                    break
                }
            }


        }

        spectrum.metaData.each { MetaDataValue value ->

            for (String option : [EXACT_MASS]) {
                if (value.getName().toLowerCase().equals(option)) {
                    exactMass = Double.parseDouble(value.getValue().toString())

                    break
                }
            }

            for (String option : [PRECURSORTYPE]) {


                if (value.getName().toLowerCase().equals(option)) {
                    precursorType = (value.getValue().toString())

                    break
                }
            }
        }

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
                    Double accuracy = Math.abs(massError)/exactMass  * 1000000
                    logger.info("accuracy mass: ${accuracy}")

                    metaDataPersistenceService.generateMetaDataObject(spectrum, [name: MASS_ACCURACY, value: accuracy, category: "mass spectrometry", unit:"ppm", computed: true])
                    metaDataPersistenceService.generateMetaDataObject(spectrum, [name: MASS_ERROR, value: massError*1000, category: "mass spectrometry", unit:"mDa", computed: true])

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