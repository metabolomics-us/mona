package moa.server.curation
import curation.CurationObject
import curation.CurationWorkflow
import grails.plugin.cache.CacheEvict
import grails.transaction.Transactional
import moa.Compound
import moa.News
import moa.server.NewsService

//@Transactional
class CompoundCurationService {

    CurationWorkflow compoundCurationWorkflow

    NewsService newsService
    /**
     * runs the curation workflow for the given spectra
     * @param id
     * @return
     */
    boolean validate(long id) {
        Compound c = Compound.get(id)

        if (c) {
            def result = compoundCurationWorkflow.runWorkflow(new CurationObject(c))

            newsService.createNews("compound ${c.inchiKey} validated","compound validation just finished!","/compounds/display/${c.id}",60,News.NOTIFICATION,"compound")
            return result
        }
        else{
            throw new RuntimeException("compound with ${id} was not found!")
        }
    }
}
