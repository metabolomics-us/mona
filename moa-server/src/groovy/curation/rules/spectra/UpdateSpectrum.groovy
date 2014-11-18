package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import groovy.json.JsonSlurper
import moa.Compound
import moa.Name
import moa.Spectrum
import moa.Submitter
import org.apache.log4j.Logger

/**
 * Created by sajjan on 10/17/14.
 * Only to be used when refreshing the database with external data
 */
class UpdateSpectrum extends AbstractCurationRule {
    private Logger logger = Logger.getLogger(getClass())

    // Load json data
    //static final data = new JsonSlurper().parseText(new File("/Volumes/Data/massbank/massbank_names.json").text)

    static final massbankSubmiters = [
            'BML': 'Washington State University',
            'BSU': 'Boise State University',
            'CA': 'Kyoto University',
            'CE': 'MPI for Chemical Ecology',
            'CO': 'University of Connecticut',
            'EA': 'Eawag',
            'EQ': 'Eawag',
            'FFF': 'PFOS Research Group',
            'FIO': 'CPqRR/FIOCRUZ',
            'FU': 'Fukuyama University',
            'GLS': 'GL Sciences Inc.',
            'JEL': 'JEOL Ltd.',
            'JP': 'University of Tokyo',
            'KNA': 'NAIST',
            'KO': 'Keio University',
            'KZ': 'Kazusa DNA Research Institute',
            'MCH': 'Osaka MCHRI',
            'MSJ': 'Mass Spectrometry Society of Japan',
            'MT': 'Metabolon, Inc.',
            'NU': 'Nihon University',
            'OUF': 'Osaka University',
            'PB': 'IPB Halle',
            'PR': 'RIKEN',
            'TT': 'Tottori University',
            'TY': 'University of Toyama',
            'UF': 'Helmholtz Centre for Environmental Research',
            'UO': 'University of Occupational and Environmental Health',
            'UT': 'Chubu University',
            'WA': 'Nihon Waters K.K.'
    ]

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


        // Update submitter for massbank records
        if(s.submitter.firstName == 'Gert') {
            filename = filename.replaceAll("\\s", "").substring(0, 4).replaceAll("\\d", "");

            Submitter submitter = Submitter.findOrSaveByEmailAddress(filename + '@MassBank.jp');
            submitter.firstName = massbankSubmiters[filename]
            submitter.lastName = 'Blank'
            submitter.password = 'password'
            submitter.addToSpectra(s)

            logger.info(submitter.validate())
            logger.info(submitter.errors)

            submitter.save(flush: true)
        }


        /*
        if(!data.find{it.key == filename}) {
            logger.info('Could not find filename!')
            return false
        }

        // Get new spectrum/compound data
        def newData = data[filename]

        logger.info("Filename "+ filename +" for spectrum"+ s.getId())



        // Update compound names
        for(String newName : newData) {
            logger.info('Adding name '+ newName)

            Name n = Name.findOrSaveByNameAndCompound(newName, s.biologicalCompound)
            s.biologicalCompound.addToNames(n)
            s.biologicalCompound.save(flush: true)
        }

        // Update inchis and mol files
        logger.info("Old InChIKey: "+ s.getBiologicalCompound().getInchiKey() +", New InChIKey: "+ newData.inchikey)
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
        */


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
