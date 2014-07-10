package moa.query

import grails.converters.JSON
import moa.MetaDataValue
import moa.Spectrum
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.meta.StringMetaDataValue
import org.hibernate.QueryException
import util.MetaDataValueHelper

class SpectraQueryController {

    static responseFormats = ['json']


    def beforeInterceptor = {
        log.info("$params - $request.JSON")
    }

    /**
     * search function for the query controller
     */
    def search() {

        def json = request.JSON

        /*
        def result = Spectrum.createCriteria().list() {

            //we have compound information available
            if (json.compound) {
                or {
                    biologicalCompound {

                        if (json.compound.name) {
                            names {

                                if (json.compound.name.like) {
                                    like('name', json.compound.name.like)
                                } else if (json.compound.name.eq) {
                                    eq('name', json.compound.name.eq)
                                } else {
                                    throw new QueryException("invalid query term: ${json.compound.name.eq}")
                                }
                            }
                        }
                    }
                    chemicalCompound {

                        if (json.compound.name) {
                            names {

                                if (json.compound.name.like) {
                                    like('name', json.compound.name.like)
                                } else if (json.compound.name.eq) {
                                    eq('name', json.compound.name.eq)
                                } else {
                                    throw new QueryException("invalid query term: ${json.compound.name.eq}")
                                }
                            }
                        }
                    }
                }
            }

            //we have metaData queries
            if (json.metaData) {

                //go over each metadata definition
                json.metaData.eachWithIndex { current, index ->

                    metaData {

                        //our metadata values

                        //our meta data parent object which has to have a specific name
                        metaData {
                            eq("name", current.name)
                        }

                        //equals condition and calculate the actual implementation
                        if (current.value.eq) {
                            def impl = estimateMetaDataValueImpl(current.value.eq.toString())
                            //our actual value
                            eq(impl.name, impl.value)
                        } else {
                            throw new QueryException("invalid query term: ${current.value.eq}")
                        }
                    }
                }
            }
        }

*/

        //completed query string
        String queryOfDoom = "select s from Spectrum s "

        //defines all our joins
        String queryOfDoomJoins = ""

        //defines our where clause
        String queryOfDoomWhere = " where "

        //our defined execution parameters
        def executionParams = [:]

        //if we have a compound
        if (json.compound) {

            //if we have a compound name
            if (json.compound.name) {

                queryOfDoomJoins += " left join s.biologicalCompound.names as bc"
                queryOfDoomJoins += " left join s.chemicalCompound.names as cc"

                //if we have a like condition specified
                if (json.compound.name.like) {
                    queryOfDoomWhere += "(bc.name like :compoundName or cc.name like :compoundName)"
                    executionParams.compoundName = json.compound.name.like
                }

                //if we have an equals condition specified
                else if (json.compound.name.eq) {
                    queryOfDoomWhere += "(bc.name = :compoundName or cc.name = :compoundName)"
                    executionParams.compoundName = json.compound.name.eq

                }
                //well we don't know this, do we?
                else {
                    throw new QueryException("invalid query term: ${json.compound.name}")
                }
            }
        }


        //if we have a metadata object specified
        if (json.metaData) {

            //go over each metadata definition
            json.metaData.eachWithIndex { current, index ->
                def impl = [:];

                //figure out the correct value for equals
                if (current.value.eq) {
                    impl = estimateMetaDataValueImpl(current.value.eq.toString())
                } else {
                    throw new QueryException("invalid query term: ${current.value.eq}")
                }

                //build the join for each metadata object link
                queryOfDoomJoins += " left join s.metaData as mdv_${index}"
                queryOfDoomJoins += " left join mdv_${index}.metaData as md_${index}"

                //if there is something in the where clause we need an and
                if(!queryOfDoomWhere.equals(" where ")){
                    queryOfDoomWhere+=" and "
                }

                //add the where clause
                queryOfDoomWhere += "("
                queryOfDoomWhere += " md_${index}.name = :metaDataName_${index}"
                queryOfDoomWhere += " and "
                queryOfDoomWhere += " mdv_${index}.${impl.name} = :metaDataImplValue_${index}"
                queryOfDoomWhere += ")"

                executionParams.put("metaDataName_${index}".toString(), current.name);
                executionParams.put("metaDataImplValue_${index}".toString(), impl.value);

            }
        }

        //assemble the query of doom
        queryOfDoom = queryOfDoom + queryOfDoomJoins + queryOfDoomWhere

        log.info("generated doom query: \n\n${queryOfDoom}\n\n")
        log.info("parameter matrix:\n\n ${executionParams}")

        def result = Spectrum.executeQuery(queryOfDoom, executionParams)

        log.info("received results from query: ${result.size()}")
        render(result as JSON)
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
