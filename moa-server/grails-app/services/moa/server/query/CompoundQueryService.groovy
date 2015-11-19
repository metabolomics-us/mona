package moa.server.query

import grails.transaction.Transactional
import moa.Compound

@Transactional
class CompoundQueryService {

    /**
     * general servivce to query compounds
     * @param query
     */
    def query(Map query = [:],Map params = [:]) {

        def ids = Compound.executeQuery("select distinct c.id from Compound c",[],params);

        def result = Compound.findAllByIdInList(ids)

        return result;
    }
}