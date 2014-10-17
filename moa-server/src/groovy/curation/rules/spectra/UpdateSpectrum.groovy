package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import groovy.json.JsonSlurper
import moa.Compound
import moa.Spectrum
import org.apache.log4j.Logger

/**
 * Created by sajjan on 10/17/14.
 * Only to be used when refreshing the database with external data
 */
class UpdateSpectrum extends AbstractCurationRule {
    private Logger logger = Logger.getLogger(getClass())

    // Load json data
    static final data = new JsonSlurper().parseText(new File("/Volumes/Data/massbank/massbank.json").text)


    @Override
    boolean executeRule(CurationObject toValidate) {
        Spectrum s = toValidate.getObjectAsSpectra()


        // Get massbank origin filename
        String filename = null;

        for(metaData in s.getMetaData()) {
            if(metaData.getName() == "origin") {
                filename = metaData.getValue();
                break;
            }
        }

        // Fail this curation step if no origin is given or if it does not exist in the data table
        if(filename == null) {
            logger.info('No filename for spectrum '+ s.getId())
            return false
        }

        if(!data.find{it.key == filename}) {
            logger.info('Could not find filename!')
            return false
        }

        // Get new spectrum/compound data
        def newData = data[filename]

        logger.info("Filename "+ filename +" for spectrum"+ s.getId())
        logger.info("Old InChIKey: "+ s.getBiologicalCompound().getInchiKey() +", New InChIKey: "+ newData.inchikey)
        logger.info("New InChI: "+ newData.inchi)
        logger.info("Old molFile: "+ s.getBiologicalCompound().getMolFile().length() +", New molFile: "+ newData.mol.length())

        // Update the biological/chemical compound and save it
        Compound c = Compound.findOrCreateByInchiKey(newData.inchikey)
        c.molFile = newData.mol
        c.inchi = newData.inchi

        c.save(flush: true)

        // Update our spectrum object with the possibly new biological/chemical compound
        s.biologicalCompound = c
        s.chemicalCompound = c
        s.save(flush: true)

        return true
    }


    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "Updates the spectrum objects in the database based with external data"
    }
}
