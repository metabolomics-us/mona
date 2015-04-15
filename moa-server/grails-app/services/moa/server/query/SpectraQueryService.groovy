package moa.server.query
import grails.transaction.Transactional
import groovy.sql.Sql
import moa.Spectrum
import moa.Tag
import moa.server.statistics.StatisticsService
import org.hibernate.QueryException
import util.query.QueryHelper

class SpectraQueryService {

    def dataSource

    static transactional = false

    MetaDataQueryService metaDataQueryService

    StatisticsService statisticsService

    @Transactional
    def query(long id) {
        return Spectrum.get(id)
    }

    /**
     * returns a list of similar spectra and similarity scores
     * @param massSpectra massspectra in a standard ion:intensity ion:intensity format
     * @param minSimilarity minimum similarity required
     * @param countTopIons how many of the ions largest ions have to be shared to be considered a hit
     * @param maxResults how many results do we maximal want to have
     */
    def findSimilarSpectraIds(String massSpectra, double minSimilarity = 500, int countTopIons = 5, int maxResults = 10) {

        log.info("spectra: ${massSpectra}")
        log.info("similarity: ${minSimilarity}")
        log.info("top ions: ${countTopIons}")
        log.info("max results: ${maxResults}")


        log.info("start searching...")
        Sql sql = new Sql(dataSource)

        long begin = System.currentTimeMillis()
        def resultList = []

        sql.eachRow("select similarity, id from findSimularSpectra(?,?,?,?) a", [massSpectra, minSimilarity, countTopIons, maxResults]) { row ->

            def hit = [:]
            hit.id = row.id
            hit.similarity = row.similarity

            resultList.add(hit)
        }

        log.info("finished search and found ${resultList.size()} hits")

        log.info("hits:\n ${resultList}")

        statisticsService.acquire(System.currentTimeMillis() - begin,"similarity search","search duration","search")
        return resultList
    }

    /**
     * returns a list of similar spectra and similarity scores
     * @param id mona spectrum id
     * @param minSimilarity minimum similarity required
     * @param countTopIons how many of the ions largest ions have to be shared to be considered a hit
     * @param maxResults how many results do we maximal want to have
     */
    def findSimilarSpectraIds(long id, double minSimilarity = 500, int countTopIons = 5, int maxResults = 10) {
        Sql sql = new Sql(dataSource)
        long begin = System.currentTimeMillis()

        def resultList = []

        sql.eachRow("select similarity, id from findSimularSpectra(?,?,?,?) a", [id, minSimilarity, countTopIons, maxResults]) { row ->

            def hit = [:]
            hit.id = row.id
            hit.similarity = row.similarity

            resultList.add(hit)
        }


        statisticsService.acquire(System.currentTimeMillis() - begin,"similarity search","search duration","search")
        return resultList
    }

    /**
     * returns a list of spectra data based on the given query
     * @param json
     */
    @Transactional
    def query(Map json, int limit = -1, int offset = -1) {
        log.info("received query: ${json}")

        long begin = System.currentTimeMillis()

        def params = [:]

        if (limit != -1) {
            params.max = limit
        }

        if (offset != -1) {
            params.offset = offset
        }

//        log.debug("pagination parameters: \n\n ${params}")

        def queryOfDoom = null
        def executionParams = null

        (queryOfDoom, executionParams) = generateFinalQuery(json)

        def result = Spectrum.executeQuery(queryOfDoom, executionParams, params)

        log.debug("result count: ${result.size()}")


        statisticsService.acquire(System.currentTimeMillis() - begin,"text search","${json}","search")

        return result
    }

    /**
     * generates the actual query to be executed for us
     * @param json
     * @return
     */
    private List generateFinalQuery(Map json) {

        //completed query string
        String queryOfDoom = "select distinct s from Spectrum s "

        //defines all our joins
        String queryOfDoomJoins = ""

        //defines our where clause
        String queryOfDoomWhere = ""

        //our defined execution parameters
        def executionParams = [:]

        (queryOfDoomWhere, queryOfDoomJoins) = handleJsonCompoundField(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        (queryOfDoomWhere, queryOfDoomJoins) = handleSpectraJsonMetadataFields(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        (queryOfDoomWhere, queryOfDoomJoins) = handleJsonTagsField(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        //assemble the query of doom
        queryOfDoom = queryOfDoom + queryOfDoomJoins + queryOfDoomWhere

        log.debug("generated query: \n\n${queryOfDoom}\n")
        log.debug("parameter matrix:\n\n${executionParams}\n\n")

        return [queryOfDoom, executionParams]
    }

    @Transactional
    def query(def json, def params) {

        if (!params.max) {
            params.max = -1
        }

        if (!params.offset) {
            params.offset = -1
        }

        if (json == null) {
	        throw new QueryException("your query needs to contain some parameters!")
        }


        return query(json, params.max as int, params.offset as int)
    }

    /**
     * does searches by tag field
     * @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleJsonTagsField(Map json, String queryOfDoomWhere, String queryOfDoomJoins, Map executionParams) {
//handling tags
        if (json.tags) {

            if (json.tags.length() > 0) {

                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)


                json.tags.eachWithIndex { current, index ->

                    //add our tag join
                    queryOfDoomJoins += " left join s.tags as t_${index}"

                    //build our specific query
                    queryOfDoomWhere += " t_${index}.text = :tag_${index}"

                    executionParams.put("tag_${index}".toString(), current.toString());

                    if (index < json.tags.length() - 1) {
                        queryOfDoomWhere += " and "
                    }
                }


            }
        }

        [queryOfDoomWhere, queryOfDoomJoins]

    }

    /**
     * does the searches by metadata field
     * @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleSpectraJsonMetadataFields(Map json, String queryOfDoomWhere, String queryOfDoomJoins, Map executionParams) {
//if we have a metadata object specified
        if (json.metadata) {

            if (json.metadata.size() > 0) {
                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                //go over each metadata definition
                json.metadata.eachWithIndex { Map current, int index ->
                    def impl = [:];

                    //build the join for each metadata object link
                    queryOfDoomJoins += " left join s.metaData as mdv_${index}"
                    queryOfDoomJoins += " left join mdv_${index}.metaData as md_${index}"
                    queryOfDoomJoins += " left join md_${index}.category as mdc_${index}"


                    queryOfDoomWhere = metaDataQueryService.buildMetadataQueryString(queryOfDoomWhere, current, executionParams, "md_${index}", "mdv_${index}", "mdc_${index}", index)

                }
            }
        }

        [queryOfDoomWhere, queryOfDoomJoins]
    }

    /**
     * does the searches for us after compounds
     * @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleJsonCompoundField(Map json, String queryOfDoomWhere, String queryOfDoomJoins, Map executionParams) {
        log.info("incomming query in compound method:\n\n$queryOfDoomWhere\n\n")

//if we have a compound
        if (json.compound) {

            //TODO NEEDS TO BE MORE DYNAMIC

	        if (json.compound.name) {

                queryOfDoomJoins += " left join s.biologicalCompound.names as bcn"
                queryOfDoomJoins += " left join s.chemicalCompound.names as ccn"
                queryOfDoomJoins += " left join s.predictedCompound.names as pcn"

                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

		        (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField(queryOfDoomWhere, "name", [json.compound.name.entrySet().value[0]], json.compound.name.keySet()[0], executionParams, 0, "bcn")
		        (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "name", [json.compound.name.entrySet().value[0]], json.compound.name.keySet()[0], executionParams, 0, "ccn")
		        (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "name", [json.compound.name.entrySet().value[0]], json.compound.name.keySet()[0], executionParams, 0, "pcn")
            }

            //if we have an inchi key
            if (json.compound.inchiKey) {

                queryOfDoomJoins += " left join s.biologicalCompound as bc"
                queryOfDoomJoins += " left join s.chemicalCompound as cc"
                queryOfDoomJoins += " left join s.predictedCompound as pc"

                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

	            (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField(queryOfDoomWhere, "inchiKey", [json.compound.inchiKey.entrySet().value[0]], json.compound.inchiKey.keySet()[0], executionParams, 0, "bc")
	            (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "inchiKey", [json.compound.inchiKey.entrySet().value[0]], json.compound.inchiKey.keySet()[0], executionParams, 0, "cc")
	            (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "inchiKey", [json.compound.inchiKey.entrySet().value[0]], json.compound.inchiKey.keySet()[0], executionParams, 0, "pc")

            }

//if we have an id key
            if (json.compound.id) {

                queryOfDoomJoins += " left join s.biologicalCompound as bc"
                queryOfDoomJoins += " left join s.chemicalCompound as cc"
                queryOfDoomJoins += " left join s.predictedCompound as pc"

                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

	            (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField(queryOfDoomWhere, "id", [json.compound.id.entrySet().value[0]], json.compound.id.keySet()[0], executionParams, 0, "bc")
	            (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "id", [json.compound.id.entrySet().value[0]], json.compound.id.keySet()[0], executionParams, 0, "cc")
	            (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "id", [json.compound.id.entrySet().value[0]], json.compound.id.keySet()[0], executionParams, 0, "pc")

//	            if (json.compound.id && !(json.compound.id instanceof Map)) {
//                    queryOfDoomWhere += "(bc.id = :compund_id or cc.id = :compund_id or pc.id = :compund_id)"
//                    executionParams.compund_id = json.compound.id as long
//                } else if (json.compound.id.eq) {
//                    queryOfDoomWhere += "(bc.id = :compund_id or cc.id = :compund_id or pc.id = :compund_id)"
//                    executionParams.compund_id = json.compound.id.eq as long
//
//                } else {
////                    throw new QueryException("invalid query term: ${json.compound.id}")
//		            log.error("whats this dude? ${json.compound.id}")
//                }

            }

            //if we have metadata
            if (json.compound.metadata) {

                if (json.compound.metadata.size() > 0) {
                    queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                    //go over each metadata definition
                    json.compound.metadata.eachWithIndex { Map current, int index ->
                        def impl = [:];

                        //build the join for each metadata object link
                        queryOfDoomJoins += " left join s.biologicalCompound.metaData as cmdv_${index}"
                        queryOfDoomJoins += " left join cmdv_${index}.metaData as cmd_${index}"
                        queryOfDoomJoins += " left join cmd_${index}.category as cmdc_${index}"


                        queryOfDoomWhere = metaDataQueryService.buildMetadataQueryString(queryOfDoomWhere, current, executionParams, "cmd_${index}", "cmdv_${index}", "cmdc_${index}", index)

                    }
                }
            }

        }




        [queryOfDoomWhere, queryOfDoomJoins]
    }

    /**
     * takes care of figuring out if we need a where or an and for the current query
     * @param queryOfDoomWhere
     * @return
     */
    private String handleWhereAndAnd(String queryOfDoomWhere) {

        if (queryOfDoomWhere.empty) {
            log.info("using where!")
            queryOfDoomWhere += " where "
        } else {
            log.info("using and!")
            queryOfDoomWhere += " and "
        }

        return queryOfDoomWhere
    }

    /**
     * queries the system and updates all the values, based on the payload
     * @param json
     */
    def update(queryContent, update) {
        def result = query(queryContent);

        //go over all spectra
        result.each { Spectrum spectrum ->

            //if we have tags specified
            if (update.tags) {
                update.tags.each { String t ->

                    //if a tag starts with minus we want to remove it
                    if (t.startsWith("-")) {

                        String nameToDelete = t.substring(1, t.length())
                        def deleteMe = []
                        spectrum.tags.each {

                            if (it.text == nameToDelete) {
                                deleteMe.add(it)
                            }
                        }

                        deleteMe.each { Tag tag ->
                            log.info("removing tag from spectra: ${tag}")
                            spectrum.removeFromTags(tag)
                        }
                    }
                    //else we want to add it
                    else {
                        Tag tag = Tag.findOrSaveByText(t);


                        spectrum.addToTags(tag)
                    }
                }
            }

            //save the now modified spectra
            spectrum.save(flush: true)
        }

        return [updated: result.size()]
    }

    /**
     * delete the result of the given query, which can take a while.
     * @param deleteQuery
     * @return
     */
    def searchAndDelete(def deleteQuery) {

        log.info("query system for delete request: ${deleteQuery}")

        def queryOfDoom = null
        def executionParams = null

        (queryOfDoom, executionParams) = generateFinalQuery(deleteQuery)


        def result = Spectrum.executeQuery(queryOfDoom, executionParams)

        log.info("have ${result.size()} spectra to remove in this batch")
        result.each { Spectrum spectrum ->
            log.info("deleting spectrum: ${spectrum.id}")
            spectrum.delete(flush: true)
        }

        log.info("finished delete operation")
    }

}
