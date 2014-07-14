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

                //equals
                if (current.value.eq != null) {
                    impl = estimateMetaDataValueImpl(current.value.eq.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} = :metaDataImplValue_${index}"

                    executionParams.put("metaDataImplValue_${index}".toString(), impl.value);

                }
                //like
                else if (current.value.like != null) {
                    impl = estimateMetaDataValueImpl(current.value.like.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} like :metaDataImplValue_${index}"

                    executionParams.put("metaDataImplValue_${index}".toString(), "%${impl.value}%");
                    executionParams.put("metaDataImplValue_${index}".toString(), "%${impl.value}%");

                }
                //greater than
                else if (current.value.gt != null) {
                    impl = estimateMetaDataValueImpl(current.value.gt.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} > :metaDataImplValue_${index}"
                    executionParams.put("metaDataImplValue_${index}".toString(), impl.value);

                }
                //less than
                else if (current.value.lt != null) {
                    impl = estimateMetaDataValueImpl(current.value.lt.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} < :metaDataImplValue_${index}"
                    executionParams.put("metaDataImplValue_${index}".toString(), impl.value);

                }

                //greate equals
                else if (current.value.ge != null) {
                    impl = estimateMetaDataValueImpl(current.value.ge.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} >= :metaDataImplValue_${index}"
                    executionParams.put("metaDataImplValue_${index}".toString(), impl.value);

                }

                //less equals
                else if (current.value.le != null) {
                    impl = estimateMetaDataValueImpl(current.value.le.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} <= :metaDataImplValue_${index}"
                    executionParams.put("metaDataImplValue_${index}".toString(), impl.value);

                }

                //not equals
                else if (current.value.ne != null) {
                    impl = estimateMetaDataValueImpl(current.value.ne.toString())

                    //equality search
                    queryOfDoomWhere += " mdv_${index}.${impl.name} != :metaDataImplValue_${index}"
                    executionParams.put("metaDataImplValue_${index}".toString(), impl.value);

                }

                //between
                else if (current.value.between != null) {
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

                }
                //unsupported term
                else {
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
