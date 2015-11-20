package curation.rules.meta

import curation.AbstractCurationRule
import curation.CurationObject
import moa.MetaData
import moa.MetaDataValue
import moa.Spectrum
import moa.server.metadata.MetaDataPersistenceService

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 7/29/15
 * Time: 2:40 PM
 */
class RemapMetadataNames extends AbstractCurationRule {

    Map<String, String> mapping = new HashMap<>()

    MetaDataPersistenceService metaDataPersistenceService

    @Override
    boolean executeRule(CurationObject toValidate) {
        Spectrum spectrum = toValidate.getObjectAsSpectra();

        List<MetaDataValue> toRemap = []

        spectrum.listAvailableValues().each {
            if (!it.isComputed()) {
                if (mapping.containsKey(it.getName())) {
                    toRemap.add(it)
                }
            }
        }

        toRemap.each {
            logger.info("remapping from ${it.getName()} to ${mapping.get(it.getName())}")
            String newName = mapping.get(it.getName())

            metaDataPersistenceService.generateMetaDataObject(spectrum, [hidden: it.getHidden(), name: newName, value: it.getValue(), unit: it.getUnit(), category: it.getCategory()])

            metaDataPersistenceService.removeMetaDataValue(it)
        }

        return true
    }


    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "utilized to remap metadata names of this spectra to preferred ones"
    }
}
