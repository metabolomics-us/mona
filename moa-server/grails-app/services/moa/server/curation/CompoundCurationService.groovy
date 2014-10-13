package moa.server.curation
import curation.CurationObject
import curation.CurationWorkflow
import grails.plugin.cache.CacheEvict
import grails.transaction.Transactional
import moa.Compound

@Transactional
class CompoundCurationService {

    CurationWorkflow compoundCurationWorkflow

    /**
     * runs the curation workflow for the given spectra
     * @param id
     * @return
     */
    @CacheEvict(value='compound', allEntries=true)
    boolean validate(long id) {
        Compound c = Compound.get(id)

        return compoundCurationWorkflow.runWorkflow(new CurationObject(c))
    }
}
