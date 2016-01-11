package curation.association

import curation.AbstractCurationRule
import curation.CurationObject
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import moa.Compound
import moa.Name
import moa.Spectrum
import moa.Submitter
import moa.Tag
import org.apache.log4j.Logger
import org.hibernate.exception.LockAcquisitionException
import util.FireJobs
import util.MetaDataFieldNames

/**
 * Created by sajjan on 10/17/14.
 * Only to be used when refreshing the database with external data
 */
class AssociatesMassBankSpectraWithCorrectSubmitter extends AbstractAssociationRule {
    private Logger logger = Logger.getLogger(getClass())

    // Load json data
    //static final data = new JsonSlurper().parseText(new File("/Volumes/Data/massbank/massbank_names.json").text)

    static final massbankSubmiters = [
            'BML': 'Washington State University',
            'BSU': 'Boise State University',
            'CA' : 'Kyoto University',
            'CE' : 'MPI for Chemical Ecology',
            'CO' : 'University of Connecticut',
            'EA' : 'Eawag',
            'EQ' : 'Eawag',
            'FFF': 'PFOS Research Group',
            'FIO': 'CPqRR/FIOCRUZ',
            'FU' : 'Fukuyama University',
            'GLS': 'GL Sciences Inc.',
            'JEL': 'JEOL Ltd.',
            'JP' : 'University of Tokyo',
            'KNA': 'NAIST',
            'KO' : 'Keio University',
            'KZ' : 'Kazusa DNA Research Institute',
            'MCH': 'Osaka MCHRI',
            'MSJ': 'Mass Spectrometry Society of Japan',
            'MT' : 'Metabolon, Inc.',
            'NU' : 'Nihon University',
            'OUF': 'Osaka University',
            'PB' : 'IPB Halle',
            'PR' : 'RIKEN',
            'TT' : 'Tottori University',
            'TY' : 'University of Toyama',
            'UF' : 'Helmholtz Centre for Environmental Research',
            'UO' : 'University of Occupational and Environmental Health',
            'UT' : 'Chubu University',
            'WA' : 'Nihon Waters K.K.'
    ]

    @Override
    boolean executeRule(CurationObject toValidate) {
        Spectrum s = toValidate.getObjectAsSpectra()

        // Get massbank origin filename
        String filename = null;

        String authors = null;
        boolean isMB = false;

        for (metaData in s.getMetaData()) {
            if (metaData.getName() == MetaDataFieldNames.ORIGIN) {
                filename = metaData.getValue();
            } else if (metaData.getName() == MetaDataFieldNames.ACCESSION) {
                filename = metaData.getValue();
            }

            if (metaData.getName() == MetaDataFieldNames.AUTHORS) {
                authors = metaData.getValue();

                for (Tag tag : s.getTags()) {
                    if (tag.text.equalsIgnoreCase("massbank")) {
                        isMB = true;
                    }
                }
            }

        }

        // Fail this curation step if no origin is given or if it does not exist in the data table
        if (filename == null && !isMB) {
            logger.info("No filename for spectrum ${s.getId()}")
            return false
        }


        logger.info("filename: ${filename}")
        if (filename != null) {
            filename = filename.replaceAll("\\s", "").substring(0, 4).replaceAll("\\d", "");
        }

        if (isMB || massbankSubmiters[filename] != null) {
            Spectrum.withTransaction {
                try {
                    associate(authors, s, filename)
                } catch (LockAcquisitionException e) {
                    FireJobs.fireSpectraAssociationJob([spectraId: s.id])
                }
            }
        } else {
            logger.warn("not a massbank file: ${filename}")
        }

        return true
    }

    private void associate(String authors, Spectrum s, String filename) {

        Submitter submitter = Submitter.findOrCreateByEmailAddress(filename + '@MassBank.jp');

        if (submitter.emailAddress != s.submitter.emailAddress) {
            if (!submitter.validate()) {
                logger.info("creating a new submitter")

                //first last name
                if (authors.contains(",")) {
                    //try to get the first name out of the authors field
                    def names = authors.split(",");

                    if (names.length > 0) {
                        names = names[0].split(" ")

                        if (names.length >= 2) {
                            submitter.firstName = names[0]
                            submitter.lastName = names[1]
                        } else {
                            submitter.firstName = "none";
                            submitter.lastName = names[0];
                        }
                    }
                }
                //no known pattern
                else {
                    submitter.firstName = authors;
                    submitter.lastName = authors;
                }


                if (filename == null) {
                    submitter.institution = authors
                } else {
                    submitter.institution = massbankSubmiters[filename]
                }

                submitter.password = "password-${System.currentTimeMillis()}"
                submitter.accountEnabled = false

                if (!submitter.validate()) {
                    logger.error(submitter.errors)
                }
                submitter.save()


                logger.debug("submitter is created: ${submitter}")
            } else {
                logger.debug("submitter already exists: ${submitter}")
            }

            Submitter current = s.submitter

            logger.debug("detaching spectra from current submitter: ${current}")
            current.removeFromSpectra(s)

            s.submitter = null
            s.save()
            current.save()

            logger.debug("attaching new submitter to spectra")
            submitter.addToSpectra(s).save()
            s.submitter = submitter

            logger.debug("saving spectra")
            s.save()
        }
    }

    @Override
    String getDescription() {
        return "Updates the spectrum objects in the database based with external data"
    }
}
