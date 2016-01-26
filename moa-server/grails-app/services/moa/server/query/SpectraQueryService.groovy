package moa.server.query

import curation.CommonTags
import edu.ucdavis.fiehnlab.spectra.hash.core.SplashFactory
import edu.ucdavis.fiehnlab.spectra.hash.core.types.Ion
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SpectraUtil
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.sql.Sql
import groovy.time.TimeCategory
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
    def findSimilarSpectraIds(String massSpectra, double minSimilarity = 500, int maxResults = 10) {
        String histogram = SplashUtil.splash(massSpectra, SpectraType.MS).split("-")[3]

        log.info("start searching...")
        Sql sql = new Sql(dataSource)

        long begin = System.currentTimeMillis()
        def resultList = new LinkedHashSet()

        sql.eachRow(" select  spectrum_id as id, calculatesimilarity(?,spectrum_id) as similarity from splash a, spectrum b where a.spectrum_id = b.id and b.deleted = false and (10-levenshtein(block2,:?))/10 > 0.9 and calculatesimilarity(?,spectrum_id) > ? limit ?", [massSpectra, histogram, massSpectra, minSimilarity, maxResults]) { row ->
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
    def findSimilarSpectraIds(long id, double minSimilarity = 500, int maxResults = 10) {
        Spectrum spectrum = Spectrum.get(id)
        String histogram = spectrum.splash.block2

        log.info("start searching...")
        Sql sql = new Sql(dataSource)

        long begin = System.currentTimeMillis()
        def resultList = []

        sql.eachRow(" select  spectrum_id as id, calculatesimilarity(?,spectrum_id) as similarity from splash a, spectrum b where a.spectrum_id = b.id and b.deleted = false and (10-levenshtein(block2,?))/10 > 0.9 and calculatesimilarity(?,spectrum_id) > ?  limit ?", [id, histogram, id, minSimilarity, maxResults]) { row ->
            def hit = [:]
            hit.id = row.id
            hit.similarity = row.similarity

            if(row.id != id) {
                resultList.add(hit)
            }
        }

        log.info("finished search and found ${resultList.size()} hits")

        log.info("hits:\n ${resultList}")

        statisticsService.acquire(System.currentTimeMillis() - begin, "similarity search", "search duration", "search")
        return resultList
    }

    /**
     * returns a list of recently updated spectra
     * @param lastUpdated
     */
    def findIdsBylastUpdated(Date lastUpdated) {
        use(TimeCategory) {
            // Take into account issues with lastUpdated times being set before starting the curation task
            def timestamp = (lastUpdated - 30.seconds).toTimestamp().toString()

            return Spectrum.executeQuery("SELECT s.id FROM Spectrum s, SupportsMetaData m WHERE s.id = m.id AND m.lastUpdated > '$timestamp'")
        }
    }

    /**
     * returns a list of spectra data based on the given query
     * @param json
     */
    @Transactional
    def query(Map json, int limit = -1, int offset = -1, String order = "order by s.score.scaledScore desc") {
        log.info("received query: ${json}")

        def ids = queryForIds(json, limit, offset)

        try {
            if (ids.isEmpty()) {
                return []
            }

            def result = Spectrum.executeQuery("""
                    select s
                        from Spectrum as s
                        where s.id in (:ids)
                        """ + order, [ids: ids.collect({
                it.id
            })] , [readOnly: true])

            //add known fields to the spectrum
            result.each { Spectrum s ->
                ids.each { Map m ->

                    if (s.id.equals(m.id)) {
                        m.keySet().each {
                            if (!it.equals("id"))
                                s.addQueryOption(it, m.get(it))
                        }
                    }
                }
            }

            log.debug("result count: ${result.size()}")

            //size everything down to our actual unique results, stupid hibernated...
            return (result)
        }
        catch (Exception e) {
            log.error("query: ${json}")
            log.error("${ids.size()} ids: ${ids.take(10)}${ids.size() > 10 ? '...' : ''}")
            throw e;
        }
    }

    /**
     * queries for the given id's
     * @param json
     * @param limit
     * @param offset
     * @return
     */
    @Transactional
    def queryForIds(Map json, int limit = -1, int offset = -1) {
        def params = [:]

        if (limit != -1) {
            params.max = limit

        }

        if (offset != -1) {
            params.offset = offset
        }

        params.readOnly = true

        def queryOfDoom = null
        def executionParams = null

        (queryOfDoom, executionParams) = generateFinalQuery(json, false, "s.id as id")

        return Spectrum.executeQuery(queryOfDoom, executionParams, params)
    }

    /**
     * returns the exspected count for the query
     * @param json
     * @return
     */
    Integer getCountForQuery(Map json) {
        def queryOfDoom = null
        def executionParams = null

        (queryOfDoom, executionParams) = generateFinalQuery(json, true)

        def result = Spectrum.executeQuery(queryOfDoom, executionParams)

        return result.size()
    }

    /**
     * generates the actual query to be executed for us
     * @param json
     * @params count returns the count instead of the actual objects
     * @params fields specify which fields to return
     * @return
     */
    private List generateFinalQuery(Map json, boolean count = false, String fields = "s.id as id") {
        log.debug("provided json query: \n ${json as JSON}")

        //defines all our joins
        String joins = ""

        //defines our where clause
        String where = "where s.deleted = :deleted"

        String orderBy = ""

        String group = "s.id"

        String having = ""

        //our defined execution parameters
        def executionParams = [:]

        executionParams.put("deleted", new Boolean(false))

        (where, joins, fields, orderBy, group, having) = handleJsonSpectraData(json, where, joins, executionParams, fields, orderBy, group, having)
        debugModification(joins, fields, orderBy, group, having, where, executionParams)

        (where, joins, fields, orderBy, group, having) = handleJsonCompoundField(json, where, joins, executionParams, fields, orderBy, group, having)
        debugModification(joins, fields, orderBy, group, having, where, executionParams)

        (where, joins, fields, orderBy, group, having) = handleSpectraJsonMetadataFields(json, where, joins, executionParams, fields, orderBy, group, having)
        debugModification(joins, fields, orderBy, group, having, where, executionParams)

        (where, joins, fields, orderBy, group, having) = handleJsonTagsField(json, where, joins, executionParams, fields, orderBy, group, having)
        debugModification(joins, fields, orderBy, group, having, where, executionParams)

        (where, joins, fields, orderBy, group, having) = handleJsonSubmitterField(json, where, joins, executionParams, fields, orderBy, group, having)
        debugModification(joins, fields, orderBy, group, having, where, executionParams)

        //working on the ordering
        if (orderBy.length() > 0) {
            if (orderBy.startsWith(",")) {
                orderBy = orderBy.substring(1, orderBy.length())
            }

            orderBy = "order by ${orderBy}"
        }

        //working on the grouping
        if (group.length() > 0) {
            if (group.startsWith(",")) {
                group = group.substring(1, group.length())
            }

            group = "group by ${group}"
        }

        //working on having
        if (having.length() > 0) {
            if (having.startsWith(",")) {
                having = having.substring(1, having.length())
            }
            having = "having $having"
        }

        //count negates all internally added fields since there is no point in more than 1 value to be returned
        if (count) {
            fields = "select count(s.id) from Spectrum s $joins $where $group $having"
        } else {
            //assemble
            fields = " select new map($fields) from Spectrum s $joins $where $group $having $orderBy"
        }


        log.debug("generated query: \n\n${fields}\n")
        log.debug("parameter matrix:\n\n${executionParams}\n\n")


        return [fields, executionParams]
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
    private List handleJsonSpectraData(Map json, String queryOfDoomWhere, String queryOfDoomJoins, Map executionParams, String fields, String orderBy, String group, String having) {
        if (json.id) {
            log.debug("debug analyzing id query part:\n ${json.match as JSON}")

            if (json.id instanceof Collection && json.id.size() > 0) {

                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                //handle brackets
                queryOfDoomWhere += "( "

                json.id.eachWithIndex { Object current, int index ->

                    log.info("current: ${current}")
                    //long form
                    if (current instanceof Map) {
                        if (current.value) {
                            current.value.keySet().each { String key ->
                                (queryOfDoomWhere, executionParams) = buildComparisonField(queryOfDoomWhere, "id", [current.value."${key}"], key, executionParams)
                            }
                        }
                    }
                    //short form
                    else {
                        // handle id
                        try {
                            long id = Long.parseLong(current)

                            queryOfDoomWhere += " s.id = :spectraId_${index}"
                            executionParams."spectraId_${index}" = id
                        }

                        // handle email address
                        catch (NumberFormatException e) {
                            //build our specific query
                            queryOfDoomWhere += " s.splash.splash = :spectraId_${index}"
                            executionParams."spectraId_${index}" = current
                        }

                        //add or statement
                        if (index < json.id.size() - 1) {
                            queryOfDoomWhere += " OR "
                        }
                    }
                }

                //handle brackets
                queryOfDoomWhere += " )"
            }
        }

        //these direct queries are not longer supported
        if (json.hash) {
            throw new RuntimeException("this is not longer supported!")
        }

        /**
         * matching based
         */
        if (json.match) {
            log.debug("debug analyzing match query part:\n ${json.match as JSON}")

            queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

            //handle brackets
            queryOfDoomWhere += "( "

            if (json.match.exact) {
                queryOfDoomWhere += " s.splash.splash = :matchExact"
                executionParams."matchExact" = json.match.exact
            } else if (json.match.top10) {
                queryOfDoomWhere += " s.splash.block1 = :top10"
                executionParams."top10" = json.match.top10

            }
            //simialrity based searches
            else if (json.match.histogram || json.match.spectra) {

                def histogramScore = 0.5
                def spectraScore = 0.5

                //score, which needs to be reached, by default 0.5
                if (json.match.histogramScore) {
                    histogramScore = json.match.histogramScore
                }
                if (json.match.score) {
                    spectraScore = json.match.score
                }

                //build the similarity query
                if (json.match.spectra) {

                    //if no histogram provided, we generated it on the fly, utilizing the latest splash version
                    if (!json.match.histogram) {

                        json.match.histogram = SplashUtil.splash(json.match.spectra, SpectraType.MS).split("-")[1]
                    }

                    having = "$having, spectramatch(:spectra,s.id) > ${spectraScore}"
                    executionParams."spectra" = json.match.spectra


                    fields = "$fields, spectramatch(:spectra,s.id) as spectralSimilarity"

                    orderBy = "$orderBy, spectralSimilarity DESC"

                    group = "$group, s.id"

                }

                //build the histgram query
                //queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                queryOfDoomWhere += " (10-levenstein(s.splash.block2,:histogramBlock))/10 >= ${histogramScore}"
                executionParams."histogramBlock" = json.match.histogram


                fields = "$fields, (10-levenstein(s.splash.block2,:histogramBlock))/10 as histogramSimilarity"
                orderBy = "$orderBy,histogramSimilarity DESC"
                group = "$group, s.splash.block2"

            } else {
                throw new RuntimeException("none supported arguments...")
            }

            //handle brackets
            queryOfDoomWhere += " )"
        }

        [queryOfDoomWhere, queryOfDoomJoins, fields, orderBy, group, having]
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
    private List handleJsonSubmitterField(Map json, String queryOfDoomWhere, String queryOfDoomJoins, executionParams, String fields, String orderBy, String group, String having) {
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

        [queryOfDoomWhere, queryOfDoomJoins, fields, orderBy, group, having]
    }

    /**
     *{*  tags: [
     *      tag: {*          name :  {*              "eq" : "tada"
     *}*}*  ]
     *}* does searches by tag field
     * @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleJsonTagsField(Map json, String queryOfDoomWhere, String queryOfDoomJoins, Map executionParams, String fields, String orderBy, String group, String having) {
        //handling tags
        if (json.tags) {

            log.debug("debug analyzing tag query part:\n ${json.tags as JSON}")

            if (json.tags.size() > 0) {

                json.tags.eachWithIndex { current, index ->
                    queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                    queryOfDoomJoins += "  inner join s.links as t_${index} "
                    queryOfDoomJoins += "  inner join t_${index}.tag as tag_table_${index}"

                    if (current instanceof String) {
                        (queryOfDoomWhere, executionParams) = buildComparisonField(queryOfDoomWhere, "text", [current], "eq", executionParams, index, "tag_table_${index}")
                    } else {
                        if (current.name) {
                            current.name.keySet().each { String key ->
                                (queryOfDoomWhere, executionParams) = buildComparisonField(queryOfDoomWhere, "text", [current.name."${key}"], key, executionParams, index, "tag_table_${index}")
                            }
                        }
                    }
                }

            }
        }

        [queryOfDoomWhere, queryOfDoomJoins, fields, orderBy, group, having]
    }

    /**
     * does the searches by metadata field
     * @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleSpectraJsonMetadataFields(Map json, String queryOfDoomWhere, String queryOfDoomJoins, Map executionParams, String fields, String orderBy, String group, String having) {
        //if we have a metadata object specified
        if (json.metadata) {

            log.debug("debug analyzing spectra metadata query part:\n ${json.metadata as JSON}")

            if (json.metadata.size() > 0) {
                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                //go over each metadata definition
                json.metadata.eachWithIndex { Map current, int index ->

                    //build the   join for each metadata object link
                    queryOfDoomJoins += "  inner join s.metaData as mdv_${index}"
                    queryOfDoomJoins += "  inner join mdv_${index}.metaData as md_${index}"
                    queryOfDoomJoins += "  inner join md_${index}.category as mdc_${index}"

                    (queryOfDoomWhere,executionParams) = metaDataQueryService.buildMetadataQueryString(queryOfDoomWhere, current, executionParams, "md_${index}", "mdv_${index}", "mdc_${index}", index,"spectra")

                }
            }

        }

        [queryOfDoomWhere, queryOfDoomJoins, fields, orderBy, group, having]
    }

    private void debugModification(String queryOfDoomJoins, String fields, String orderBy, String group, String having, String where, def params) {
        //log.debug("modified query: \n $queryOfDoomJoins \n $where\n $fields \n$orderBy \n$group \n$having\n$params")
    }

    /**
     * does the searches for us after compounds
     * @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleJsonCompoundField(Map json, String queryOfDoomWhere, String queryOfDoomJoins, Map executionParams, String fields, String orderBy, String group, String having) {
        log.info("incomming query in compound method:\n\n$queryOfDoomWhere\n\n")

        //if we have a compound
        if (json.compound) {

            log.debug("debug analyzing compound query part:\n ${json.compound as JSON}")

            queryOfDoomJoins += " left join s.compoundLinks as cl"
            queryOfDoomJoins += " left join cl.compound as co"


            //TODO NEEDS TO BE MORE DYNAMIC
            if (json.compound.name) {
                queryOfDoomJoins += " left join co.names as cn"
                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                queryOfDoomWhere += "("

                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField(queryOfDoomWhere, "name", [json.compound.name.entrySet().value[0]], json.compound.name.keySet()[0], executionParams, 0, "cn")
                queryOfDoomWhere += ")"

            }

            //if we have an inchi key
            if (json.compound.inchiKey) {
                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)
                queryOfDoomWhere += "("

                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField(queryOfDoomWhere, "inchiKey", [json.compound.inchiKey.entrySet().value[0]], json.compound.inchiKey.keySet()[0], executionParams, 0, "co")
                queryOfDoomWhere += ")"

            }

            //if we have an id key
            if (json.compound.id) {
                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                queryOfDoomWhere += "("

                (queryOfDoomWhere, executionParams) = QueryHelper.buildComparisonField(queryOfDoomWhere, "id", [json.compound.id.entrySet().value[0]], json.compound.id.keySet()[0], executionParams, 0, "co")

                queryOfDoomWhere += ")"
            }

            //if we have metadata
            if (json.compound.metadata) {

                if (json.compound.metadata.size() > 0) {
                    queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                    //go over each metadata definition
                    json.compound.metadata.eachWithIndex { Map current, int index ->
                        def impl = [:];

                        //build the join for each metadata object link
                        queryOfDoomJoins += " inner join co.metaData as cmdv_${index}"
                        queryOfDoomJoins += " inner join cmdv_${index}.metaData as cmd_${index}"
                        queryOfDoomJoins += " inner join cmd_${index}.category as cmdc_${index}"


                        (queryOfDoomWhere, executionParams) = metaDataQueryService.buildMetadataQueryString(queryOfDoomWhere, current, executionParams, "cmd_${index}", "cmdv_${index}", "cmdc_${index}", index,"compound")

                    }
                }
            }
        }

        [queryOfDoomWhere, queryOfDoomJoins, fields, orderBy, group, having]
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
     * {
     *     update: {
     *         tags: [
     *              tagName, // to add a new tag name
     *              -tagName // to remove this tag name
     *          ]
     *     }
     * }
     * @param json
     */
    def update(queryContent, update) {
        def result = queryForIds(queryContent);

        //go over all spectra
        result.each { def id ->

            log.info("id: ${id.class}")
            Spectrum spectrum = Spectrum.get(id)

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

            if (params.forceRemoval == true) {
                def links = []

                spectrum.links.each {
                    links.add(it)
                }

                links.each {
                    tagService.removeLink(it)
                }

                spectrum.delete(flush: true)
            } else {
                spectrum.deleted = true
                spectrum.save()
            }
            statisticsService.acquire(System.currentTimeMillis() - begin, "deleted a spectra", "${spectrum.hash}", "delete")
        }

        log.info("finished delete operation")
    }
}