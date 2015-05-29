package moa.server.query

import curation.CommonTags
import grails.transaction.Transactional
import groovy.sql.Sql
import moa.Spectrum
import moa.Tag
import moa.server.statistics.StatisticsService
import moa.server.tag.TagService
import net.sf.ehcache.search.expression.Criteria
import org.hibernate.FetchMode
import org.hibernate.QueryException
import org.hibernate.criterion.CriteriaSpecification
import util.query.QueryHelper

import static util.query.QueryHelper.buildComparisonField

class SpectraQueryService {

    def dataSource

    static transactional = false

    MetaDataQueryService metaDataQueryService

    StatisticsService statisticsService

    TagService tagService

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

        statisticsService.acquire(System.currentTimeMillis() - begin, "similarity search", "search duration", "search")
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


        statisticsService.acquire(System.currentTimeMillis() - begin, "similarity search", "search duration", "search")
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

        def ids = queryForIds(json,limit,offset)

        def result = Spectrum.findAllByIdInList(ids)

        //println "$queryOfDoom"

        //  log.debug("result count: ${result.size()}")


        statisticsService.acquire(System.currentTimeMillis() - begin, "text search", "${json}", "search")


        return result
    }

    /**
     * queries for the given id's
     * @param json
     * @param limit
     * @param offset
     * @return
     */
    @Transactional
    def queryForIds(Map json, int limit = -1, int offset = -1){

        def params = [:]

        if (limit != -1) {
            params.max = limit

        }

        if (offset != -1) {
            params.offset = offset
        }

        def queryOfDoom = null
        def executionParams = null

        (queryOfDoom, executionParams) = generateFinalQuery(json)


        return Spectrum.executeQuery(queryOfDoom, executionParams,params)

    }

    /**
     * generates the actual query to be executed for us
     * @param json
     * @return
     */
    private List generateFinalQuery(Map json) {

        //completed query string
        String queryOfDoom = "select s.id from Spectrum s"

        //defines all our joins
        String queryOfDoomJoins = ""

        //defines our where clause
        String queryOfDoomWhere = ""

        //our defined execution parameters
        def executionParams = [:]

        (queryOfDoomWhere, queryOfDoomJoins) = handleJsonSpectraData(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        (queryOfDoomWhere, queryOfDoomJoins) = handleJsonCompoundField(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        (queryOfDoomWhere, queryOfDoomJoins) = handleSpectraJsonMetadataFields(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        (queryOfDoomWhere, queryOfDoomJoins) = handleJsonTagsField(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        (queryOfDoomWhere, queryOfDoomJoins) = handleJsonSubmitterField(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        //assemble the query of doom
        queryOfDoom = queryOfDoom + queryOfDoomJoins + queryOfDoomWhere + " group by s.id"

        log.debug("generated query: \n\n${queryOfDoom}\n")
        log.debug("parameter matrix:\n\n${executionParams}\n\n")

        return [queryOfDoom, executionParams]
    }

    /**
     * works on the json spectra data itself and creates an or query based on the provided id's or hashes
     *
     *{*  id : [1235 || id : "mona-hashcode" ]
     *}* @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleJsonSpectraData(Map json, String queryOfDoomWhere, String queryOfDoomJoins, Map executionParams) {



        if (json.id) {
            if(json.id.size() > 0) {

                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                //handle brackets
                queryOfDoomWhere += "( "
                json.id.eachWithIndex { String current, int index ->

                    // handle id
                    try {
                        long id = Long.parseLong(current)

                        queryOfDoomWhere += " s.id = :spectraId_${index}"
                        executionParams."spectraId_${index}" = id
                    }

                    // handle email address
                    catch (NumberFormatException e) {
                        //build our specific query
                        queryOfDoomWhere += " s.hash = :spectraId_${index}"
                        executionParams."spectraId_${index}" = current
                    }


                    //add or statement
                    if(index < json.id.size()-1){
                        queryOfDoomWhere += " OR "
                    }
                }

                //handle brackets
                queryOfDoomWhere += " )"
            }
        }

        [queryOfDoomWhere, queryOfDoomJoins]
    }

    @Transactional
    def query(Map json, def params) {

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
     * does searches by submitter field
     * @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleJsonSubmitterField(Map json, String queryOfDoomWhere, String queryOfDoomJoins, executionParams) {

        //handling submitter
        if (json.submitter) {

            queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

            queryOfDoomJoins += "  inner join s.submitter as sub"

            // handle id
            try {
                long id = Long.parseLong(json.submitter.toString())

                queryOfDoomWhere += " sub.id = :submitterInfo"
                executionParams.submitterInfo = id
            }

            // handle email address
            catch (NumberFormatException e) {
                //build our specific query
                queryOfDoomWhere += " sub.emailAddress = :submitterInfo"
                executionParams.submitterInfo = json.submitter.toString()
            }
        }

        [queryOfDoomWhere, queryOfDoomJoins]
    }

    /**
     * {
     *  tags: [
     *      tag: {
     *          name :  {
     *              "eq" : "tada"
     *          }
     *      }
     *  ]
     *  }
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

            if (json.tags.size() > 0) {


                json.tags.eachWithIndex { current, index ->

                    queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                    queryOfDoomJoins += "  inner join s.links as t_${index} "
                    queryOfDoomJoins += "  inner join t_${index}.tag as tag_table_${index}"

                    if(current instanceof  String) {
                        (queryOfDoomWhere, executionParams) = buildComparisonField(queryOfDoomWhere, "text", [current], "eq", executionParams,index,"tag_table_${index}")
                    }
                    else{
                        if(current.name) {
                            current.name.keySet().each { String key ->
                                (queryOfDoomWhere, executionParams) = buildComparisonField(queryOfDoomWhere, "text", [current.name."${key}"], key, executionParams,index,"tag_table_${index}")
                            }
                        }
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

                    //build the   join for each metadata object link
                    queryOfDoomJoins += "  inner join s.metaData as mdv_${index}"
                    queryOfDoomJoins += "  inner join mdv_${index}.metaData as md_${index}"
                    queryOfDoomJoins += "  inner join md_${index}.category as mdc_${index}"


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


            queryOfDoomJoins += " inner join s.biologicalCompound as bc"
            queryOfDoomJoins += " inner join s.chemicalCompound as cc"
            queryOfDoomJoins += " left join s.predictedCompound as pc"

            //TODO NEEDS TO BE MORE DYNAMIC
            if (json.compound.name) {


                queryOfDoomJoins += " left join bc.names as bcn"
                queryOfDoomJoins += " left join cc.names as ccn"
                queryOfDoomJoins += " left join pc.names as pcn"

                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField(queryOfDoomWhere, "name", [json.compound.name.entrySet().value[0]], json.compound.name.keySet()[0], executionParams, 0, "bcn")
                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "name", [json.compound.name.entrySet().value[0]], json.compound.name.keySet()[0], executionParams, 0, "ccn")
                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "name", [json.compound.name.entrySet().value[0]], json.compound.name.keySet()[0], executionParams, 0, "pcn")
            }

            //if we have an inchi key
            if (json.compound.inchiKey) {

                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField(queryOfDoomWhere, "inchiKey", [json.compound.inchiKey.entrySet().value[0]], json.compound.inchiKey.keySet()[0], executionParams, 0, "bc")
                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "inchiKey", [json.compound.inchiKey.entrySet().value[0]], json.compound.inchiKey.keySet()[0], executionParams, 0, "cc")
                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField("$queryOfDoomWhere or ", "inchiKey", [json.compound.inchiKey.entrySet().value[0]], json.compound.inchiKey.keySet()[0], executionParams, 0, "pc")

            }

            //if we have an id key
            if (json.compound.id) {

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
                        queryOfDoomJoins += " inner join bc.metaData as cmdv_${index}"
                        queryOfDoomJoins += " inner join cmdv_${index}.metaData as cmd_${index}"
                        queryOfDoomJoins += " inner join cmd_${index}.category as cmdc_${index}"


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
     *
     * update json:
     *
     *
     *{*     update:{*         tags:[
     *              tagName //to add a new tagname
     *              -tagName //to remove this tagname
     *          ]
     *}*}* }
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


                        tagService.removeTagFrom(nameToDelete, spectrum)
                    }
                    //else we want to add it
                    else {
                        tagService.addTagTo(t, spectrum)
                    }
                }
            } else {
                throw new RuntimeException("sorry invalid update string: ${update}")
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
    def searchAndDelete(def deleteQuery, def params = [:]) {

        log.info("query system for delete request: ${deleteQuery}")

        def queryOfDoom = null
        def executionParams = null

        (queryOfDoom, executionParams) = generateFinalQuery(deleteQuery)


        def result = Spectrum.executeQuery(queryOfDoom, executionParams, params)

        log.info("have ${result.size()} spectra to remove in this batch")
        result.each { long id ->
            log.info("deleting spectrum: ${id}")

            long begin = System.currentTimeMillis()

            Spectrum spectrum = Spectrum.get(id)
            if(params.forceRemoval == true) {


                def links = []
                spectrum.links.each {
                    links.add(it)
                }

                links.each {
                    tagService.removeLink(it)
                }


                spectrum.delete(flush: true)
            }
            else{
                tagService.removeTagFrom(CommonTags.REQUIRES_DELETE,spectrum)
                tagService.addTagTo(CommonTags.DELETED,spectrum)
                spectrum.save()
            }
            statisticsService.acquire(System.currentTimeMillis() - begin, "deleted a spectra", "${spectrum.hash}", "delete")

        }

        log.info("finished delete operation")
    }

}
