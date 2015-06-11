package curation.rules.tree

import curation.AbstractCurationRule
import curation.CurationObject
import moa.Spectrum
import moa.server.metadata.MetaDataPersistenceService
import moa.server.query.SpectraQueryService
import util.FireJobs

/**
 * attempts to create a fragementation tree for given spectra
 * there will be different implementations of the used algorithm depending on
 * provider, since there is not defined standard
 */
class GenerateFragmentationTreesRuleForMassBank extends AbstractCurationRule{

    /**
     * attempts to build the tree up
     */
    private boolean buildTreeToTop = true

    /**
     * attempts to build the tree down
     */
    private boolean buildTreeToBottom = true


    public static final String PARENT_SCAN = "parentScan"
    SpectraQueryService spectraQueryService

    MetaDataPersistenceService metaDataPersistenceService

    @Override
    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra();

        workOnSpectra(spectrum)

        return false
    }

    private void workOnSpectra(Spectrum spectrum) {
        String accession;

        String msType;

        String parent;

        for (metaData in spectrum.getMetaData()) {
            if (metaData.getName().trim() == "accession") {
                accession = metaData.getValue();
            }
            if (metaData.getName().trim() == "tms type") {
                msType = metaData.getValue();
            }
            if (metaData.getName().trim() == "comment") {
                logger.info("checking value: " + metaData.getValue())

                def group = (metaData.getValue().toString() =~ /\[.*\]\s+([A-Z]{2}[0-9]+)/)
                if (group.size() > 0) {
                    logger.info("found it: " + metaData.getValue())
                    parent = group[0][1];
                }
            }
        }

        buildChildToParentAssociation(parent, spectrum,buildTreeToTop)
    }

    /**
     * associate the given spectrum with this parent
     * @param parent
     * @param spectrum
     */
    private void buildChildToParentAssociation(String parent, Spectrum spectrum,boolean calculateParent) {
        if(parent) {
            def spectra = spectraQueryService.queryForIds([
                    metadata: [
                            [
                                    name : "accession",
                                    value: [
                                            eq: parent
                                    ]
                            ]
                    ]
            ])

            //update the spectra to reflect that it has a parent
            if (spectra.size() > 0) {
                metaDataPersistenceService.generateMetaDataObject(spectrum, [name: PARENT_SCAN, value: spectra[0], computed: true])

                if (calculateParent) {
                    workOnSpectra(Spectrum.get(spectra[0] as long),calculateParent)
                }



            }

        }
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "this will attempt to build a spectra:parent tree, based on fragmentation information found in mass bank"
    }
}
