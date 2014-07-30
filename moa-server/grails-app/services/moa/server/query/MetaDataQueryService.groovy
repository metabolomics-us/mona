package moa.server.query

import grails.transaction.Transactional

@Transactional
class MetaDataQueryService {

    /**
     * queries metadata and returns the result as json array of metadata types
     * @param json
     */
    def queryMetaData(Map json,def params = [:]) {

        log.info("received query: ${json}")

        if(json == null){
            throw new Exception("query query needs to contain some parameters")
        }

    }
}
