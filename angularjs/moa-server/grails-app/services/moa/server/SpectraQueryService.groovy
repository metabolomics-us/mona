package moa.server

import grails.transaction.Transactional
import moa.MetaDataValue
import moa.Spectrum
import moa.Tag
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.meta.StringMetaDataValue
import org.hibernate.QueryException
import util.MetaDataValueHelper

@Transactional
class SpectraQueryService {

    /**
     * returns a list of spectra data based on the given query
     * @param json
     */
    def query(def json,def params = [:]) {
        if(json == null){
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

        //if we have a compound
        if (json.compound) {

            //if we have a compound name
            if (json.compound.name) {

                if (queryOfDoomWhere.empty) {
                    queryOfDoomWhere = " where "
                }

                queryOfDoomJoins += " left join s.biologicalCompound.names as bcn"
                queryOfDoomJoins += " left join s.chemicalCompound.names as ccn"

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

                if (queryOfDoomWhere.empty) {
                    queryOfDoomWhere = " where "
                }

                //we have alreay another term, so let's add an and
                if (!queryOfDoomWhere.equals(" where ")) {
                    queryOfDoomWhere += " and "
                }

                queryOfDoomJoins += " left join s.biologicalCompound as bc"
                queryOfDoomJoins += " left join s.chemicalCompound as cc"

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

        //if we have a metadata object specified
        if (json.metadata) {

            if (json.metadata.length() > 0) {
                if (queryOfDoomWhere.empty) {
                    queryOfDoomWhere = " where "
                }
            }

            //go over each metadata definition
            json.metadata.eachWithIndex { current, index ->
                def impl = [:];

                //if there is something in the where clause we need an and
                if (!queryOfDoomWhere.equals(" where ")) {
                    queryOfDoomWhere += " and "
                }

                //build the join for each metadata object link
                queryOfDoomJoins += " left join s.metaData as mdv_${index}"
                queryOfDoomJoins += " left join mdv_${index}.metaData as md_${index}"

                //part of searching by name of metadata object
                queryOfDoomWhere += "("
                queryOfDoomWhere += " md_${index}.name = :metaDataName_${index}"
                queryOfDoomWhere += " and "

                executionParams.put("metaDataName_${index}".toString(), current.name);

                //figure out the correct value for equals
                if (current.value.eq) {
                    impl = estimateMetaDataValueImpl(current.value.eq.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} = :metaDataImplValue_${index}"

                    executionParams.put("metaDataImplValue_${index}".toString(), impl.value);

                }

                //figure out the correct value for equals
                else if (current.value.like) {
                    impl = estimateMetaDataValueImpl(current.value.like.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} = :metaDataImplValue_${index}"

                    executionParams.put("metaDataImplValue_${index}".toString(), "%${impl.value}%");
                    executionParams.put("metaDataImplValue_${index}".toString(), "%${impl.value}%");

                }

                //searching for a value between min and max
                else if (current.value.between) {
                    if (current.value.between.length() == 2) {
                        def min = current.value.between[0].toString()
                        def max = current.value.between[1].toString()

                        impl = estimateMetaDataValueImpl(min)

                        queryOfDoomWhere += " mdv_${index}.${impl.name} between :metaDataImplValue_min_${index} and :metaDataImplValue_max_${index} "

                        executionParams.put("metaDataImplValue_min_${index}".toString(), estimateMetaDataValueImpl(min).value);
                        executionParams.put("metaDataImplValue_max_${index}".toString(), estimateMetaDataValueImpl(max).value);

                    } else {
                        throw new QueryException("invalid query term: ${current.value.between}, we need exactly 2 values")
                    }

                } else {
                    throw new QueryException("invalid query term: ${current.value}")
                }

                queryOfDoomWhere += ")"


            }
        }

        //handling tags
        if (json.tags) {

            if (json.tags.length() > 0) {

                if (queryOfDoomWhere.empty) {
                    queryOfDoomWhere = " where "
                }

                json.tags.eachWithIndex { current, index ->

                    //add our tag join
                    queryOfDoomJoins += " left join s.tags as t_${index}"

                    //if there is something in the where clause we need an and
                    if (!queryOfDoomWhere.equals(" where ")) {
                        queryOfDoomWhere += " and "
                    }

                    //build our specific query
                    queryOfDoomWhere += " t_${index}.text = :tag_${index}"

                    executionParams.put("tag_${index}".toString(), current.toString());

                }


            }
        }

        //assemble the query of doom
        queryOfDoom = queryOfDoom + queryOfDoomJoins + queryOfDoomWhere

        log.info("generated doom query: \n\n${queryOfDoom}\n\n")
        log.info("parameter matrix:\n\n ${executionParams}")

        return Spectrum.executeQuery(queryOfDoom, executionParams, params)

    }

    /**
     * queries the system and updates all the values, based on the payload
     * @param json
     */
    def update(queryContent, update) {
        def result = query(queryContent);

        //build update object

        def tagsToUpdate = []

        if (update.tags) {
            update.tags.each { t ->
                Tag tag = Tag.findOrSaveByText(t);
                tagsToUpdate.add(tag)
            }
        }
        //apply object
        result.each { Spectrum spectrum ->

            tagsToUpdate.each { Tag tag ->
                spectrum.addToTags(tag)
            }

            spectrum.save(flush: true)
        }


    }

    /**
     * returns a map with exactly two keys
     * @param content
     * @return
     */
    private Map estimateMetaDataValueImpl(String content) {

        def result = [:];

        MetaDataValue value = MetaDataValueHelper.getValueObject(content)

        if (value instanceof BooleanMetaDataValue) {

            result.name = "booleanValue"
            result.value = value.booleanValue
        } else if (value instanceof DoubleMetaDataValue) {

            result.name = "doubleValue"
            result.value = value.doubleValue
        } else if (value instanceof StringMetaDataValue) {
            result.name = "stringValue"
            result.value = value.getStringValue()
        }

        return result;
    }
}
