package moa.server.query

import grails.transaction.Transactional
import moa.Spectrum
import moa.Tag
import org.hibernate.QueryException


class SpectraQueryService {

    static transactional = false

    MetaDataQueryService metaDataQueryService

    /**
     * returns a list of spectra data based on the given query
     * @param json
     */
    @Transactional
    def query(def json, def params = [:]) {
        log.info("received query: ${json}")

        if (json == null) {
            throw new Exception("your query needs to contain some parameters!")
        }
        //completed query string
        String queryOfDoom = "select s from Spectrum s "

        //defines all our joins
        String queryOfDoomJoins = ""

        //defines our where clause
        String queryOfDoomWhere = ""

        //our defined execution parameters
        def executionParams = [:]

        (queryOfDoomWhere,queryOfDoomJoins) = handleJsonCompoundField(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        (queryOfDoomWhere,queryOfDoomJoins) = handleSpectraJsonMetadataFields(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        (queryOfDoomWhere,queryOfDoomJoins) = handleJsonTagsField(json, queryOfDoomWhere, queryOfDoomJoins, executionParams)

        //assemble the query of doom
        queryOfDoom = queryOfDoom + queryOfDoomJoins + queryOfDoomWhere

        log.info("generated query: \n\n${queryOfDoom}\n\n")
        log.info("parameter matrix:\n\n ${executionParams}")

        return Spectrum.executeQuery(queryOfDoom, executionParams, params)

    }

    /**
     * does searches by tag field
     * @param json
     * @param queryOfDoomWhere
     * @param queryOfDoomJoins
     * @param executionParams
     * @return
     */
    private List handleJsonTagsField(json, String queryOfDoomWhere, String queryOfDoomJoins, executionParams) {
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
    private List handleSpectraJsonMetadataFields(json, String queryOfDoomWhere, String queryOfDoomJoins, executionParams) {
//if we have a metadata object specified
        if (json.metadata) {

            if (json.metadata.length() > 0) {
                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                //go over each metadata definition
                json.metadata.eachWithIndex { Map current, int index ->
                    def impl = [:];

                    //build the join for each metadata object link
                    queryOfDoomJoins += " left join s.metaData as mdv_${index}"
                    queryOfDoomJoins += " left join mdv_${index}.metaData as md_${index}"
                    queryOfDoomJoins += " left join md_${index}.category as mdc_${index}"


                    queryOfDoomWhere = metaDataQueryService.buildMetadataQueryString(queryOfDoomWhere, current, executionParams, "md_${index}", "mdv_${index}","mdc_${index}", index)

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
    private List handleJsonCompoundField(json, String queryOfDoomWhere, String queryOfDoomJoins, LinkedHashMap executionParams) {
        log.info("incomming query in compound method:\n\n$queryOfDoomWhere\n\n")

//if we have a compound
        if (json.compound) {

            //if we have a compound name
            if (json.compound.name) {

                queryOfDoomJoins += " left join s.biologicalCompound.names as bcn"
                queryOfDoomJoins += " left join s.chemicalCompound.names as ccn"
                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                //if we have a like condition specified
                if (json.compound.name.like) {
                    queryOfDoomWhere += "(bcn.name like :compoundName or ccn.name like :compoundName)"
                    executionParams.compoundName = "%${json.compound.name.like}%"
                }

                //if we have an equals condition specified
                else if (json.compound.name.eq) {
                    queryOfDoomWhere += "(bcn.name = :compoundName or ccn.name = :compoundName)"
                    executionParams.compoundName = json.compound.name.eq

                }
                //well we don't know this, do we?
                else {
                    throw new QueryException("invalid query term: ${json.compound.name}")
                }
            }

            //if we have an inchi key
            if (json.compound.inchiKey) {

                queryOfDoomJoins += " left join s.biologicalCompound as bc"
                queryOfDoomJoins += " left join s.chemicalCompound as cc"
                queryOfDoomWhere = handleWhereAndAnd(queryOfDoomWhere)

                if (json.compound.inchiKey.eq) {
                    queryOfDoomWhere += "(bc.inchiKey = :inchiKey or cc.inchiKey = :inchiKey)"
                    executionParams.inchiKey = json.compound.inchiKey.eq
                } else if (json.compound.inchiKey.like) {
                    queryOfDoomWhere += "(bc.inchiKey like :inchiKey or cc.inchiKey like :inchiKey)"
                    executionParams.inchiKey = "%${json.compound.inchiKey.like}%"
                } else {
                    throw new QueryException("invalid query term: ${json.compound.inchiKey}")
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

}
